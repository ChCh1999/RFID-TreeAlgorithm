import json

1  # !/usr/bin/env python
2  # -*- coding: utf-8 -*-
3  # @Time  : 16:21
4  # @File  : data_util.py
5  # @Author: Ch
6  # @Date  : 2020/3/3

import os
import numpy as np
import re
import conf

labels = conf.Config.labels


def get_sum(data: dict):
    """
    将数据转换为递增数据
    @param data:
    @return:
    """
    res = {}
    for k, v in data.items():
        res[k] = []
        for round_data in v:
            sum_list = [round_data[0]]
            for index in range(1, len(round_data)):
                sum_list.append(round_data[index] + sum_list[index - 1])
            res[k].append(sum_list)
    return res


def sub_a_num(data: dict, bias):
    res = {}
    for k, v in data.items():
        res[k] = list(v)
        for round_data in res[k]:
            for index in range(len(round_data)):
                round_data[index] = round_data[index] - bias
    return res


def get_bias(data: dict, index=0):
    res = {}
    k0 = list(data.keys())[index]
    for k, v in data.items():
        res[k] = np.copy(v)
        for round_index in range(len(v)):
            for index in range(len(v[round_index])):
                res[k][round_index][index] = res[k][round_index][index] - data[k0][round_index][index]
    return res


def get_convolved(data: dict, level=3):
    res = {}
    for k, v in data.items():
        res[k] = []
        for round in v:
            weights = np.ones(level) / level
            convolved = np.convolve(weights, round)[level - 1:-level + 1]
            res[k].append(convolved)
    return res


def get_smoth(data: dict):
    res = {}
    for k, v in data.items():
        res[k] = []
        for round in v:
            res[k].append()


def get_abs(data: dict):
    res = {}
    for k, v in data.items():
        res[k] = list(np.abs(v))
    return res


def get_log(data: dict):
    res = {}
    for k, v in data.items():
        res[k] = list(np.log10(v))
    return res


def get_data_avg(data: dict):
    res = {}
    for k, v in data.items():
        res[k] = []
        res[k].append(np.array(v).mean(axis=0))
    return res


def get_data_mid(data: dict):
    res = {}
    for k, v in data.items():
        res[k] = []
        res[k].append(np.percentile(v, 50, axis=0))
    return res


def get_data_freq_p(data: dict):
    """
    获取众数
    @param data:{st:[[..sessin]..round]}
    @return:
    """
    res = {}
    for st, data in data.items():
        res[st] = []
        matrix = np.ndarray(data)

    return res


def get_data_rm_out_point(data: dict, iqrlimit=1.5):
    res = {}
    for k, v in data.items():
        res[k] = []
        datalist = []
        count = []
        for i in range(len(v[0])):
            datalist.append(0)
            count.append(0)

        #   计算IQR的阈值
        mid = np.percentile(v, 50, axis=0)
        q3 = np.percentile(v, 75, axis=0)
        q1 = np.percentile(v, 25, axis=0)
        iqr = np.subtract(q3, q1)
        iqr = np.multiply(iqr, iqrlimit)
        max_limit = np.add(mid, iqr)
        min_limit = np.subtract(mid, iqr)

        # 遍历去除异常值
        for rounds in v:
            for session_index in range(len(rounds)):
                record = rounds[session_index]
                if min_limit[session_index] <= record <= max_limit[session_index]:
                    datalist[session_index] += record
                    count[session_index] += 1

        for index in range(len(datalist)):
            datalist[index] = datalist[index] / count[index]
        res[k].append(datalist)
    return res


def get_data_in_dir(dir_path: str, data_keys: list):
    """
    获取文件夹中json数据
    @param data_keys: 要获取的数据的键，比如[p,pm]
    @param dir_path:
    @return: {value_key：{strategy_name:[round[session]]}}
        sample: {'p':{'random':[[0.3*20],[0.31*20]}}
    """
    data_raw = {}
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            index = re.search(r's\d', file)
            if not index or not file.find('.json'):
                continue
            strategy_index = int(index.group()[-1])
            if strategy_index < len(labels):
                with open(os.path.join(root, file)) as res:
                    if data_raw.keys().__contains__(labels[strategy_index]):
                        data_raw[labels[strategy_index]].extend(json.load(res))
                    else:
                        data_raw[labels[strategy_index]] = json.load(res)
    # data_keys = ['p', 'pm', 'slot',"CBMCount","CBMOfSameCollisionBitsCount"]
    data = {}
    for key in data_keys:
        data[key] = {}
        for k, v in data_raw.items():
            data[key][k] = []
            for round in v:
                round_data = []
                for session in round:
                    round_data.append(session[key])
                data[key][k].append(round_data)
    return data


def get_slot_pm_threshold(slot_data: dict, pm_data: dict, threshold: float):
    slot_res = {}
    pm_res = {}
    for k, _ in slot_data.items():
        slot_res[k] = []
        pm_res[k] = []
        for round_index in range(len(slot_data[k])):
            slot_res[k].append([])
            pm_res[k].append([])
            for index in range(len(pm_data[k][round_index])):
                if pm_data[k][round_index][index] <= threshold:
                    pm_res[k][round_index].append(pm_data[k][round_index][index])
                    slot_res[k][round_index].append(slot_data[k][round_index][index])

    return slot_res, pm_res


def get_slot_pm_threshold_2n(dir_path: str):
    """
    获取一组以0.2/i^n为阈值的slot数据，第i存放在dir_path/i文件夹内
    @param dir_path: 文件夹目标
    @return:res_slot, res_threshold_pm
    """
    res_slot = {}
    res_threshold_pm = {}

    for k in labels:
        res_slot[k] = []
        res_slot[k].append([])

    for i in range(10):
        data_temp = get_data_in_dir(dir_path + "/" + str(i), ["slot"])["slot"]
        data_temp = get_sum(data_temp)
        for k, v in data_temp.items():
            for index in range(len(v)):
                v[index] = [v[index][-1]]
        data_temp = get_data_avg(data_temp)
        for k in labels:
            res_slot[k][0].append(data_temp[k][0][-1])

    list_threshold = []
    threshold = 0.2
    for i in range(10):
        list_threshold.append(threshold)
        threshold = threshold / 2
    for k in labels:
        res_threshold_pm[k] = []
        res_threshold_pm[k].append(list_threshold.copy())

    return res_slot, res_threshold_pm


def get_slot_pm_threshold(dir_path: str):
    """
    获取以阈值为标签的文件夹数据
    @param dir_path: 总文件夹目录，字目录均为阈值
    @return: 时隙数据、阈值数据
    """
    res_slot = {}
    res_threshold_pm = {}

    # for k in labels:
    #     res_slot[k] = []
    #     res_slot[k].append([])
    threshold_dir_list = os.listdir(dir_path)
    list_threshold = []
    num_match = re.compile(r'^([0-1].\d*)|0$')
    # num_match = re.compile(r'^-?([0-1].\d*)|0$')
    for i in range(len(threshold_dir_list)):
        if not num_match.match(threshold_dir_list[i]):
            continue
        else:
            threshold = float(threshold_dir_list[i])
            if threshold < 0 or threshold > 1:
                continue
        list_threshold.append(threshold)
        print(os.path.join(dir_path, threshold_dir_list[i]))
        data_temp = get_data_in_dir(os.path.join(dir_path, threshold_dir_list[i]), ["slot"])["slot"]
        data_temp = get_sum(data_temp)
        for k, v in data_temp.items():
            for index in range(len(v)):
                v[index] = [v[index][-1]]
        data_temp = get_data_avg(data_temp)
        for k in data_temp.keys():
            if k in res_slot.keys():
                res_slot[k][0].append(data_temp[k][0][-1])
            else:
                res_slot[k] = []
                res_slot[k].append([])
                res_slot[k][0].append(data_temp[k][0][-1])

    for k in labels:
        if k in res_slot.keys():
            res_threshold_pm[k] = []
            res_threshold_pm[k].append(list_threshold.copy())

    return res_slot, res_threshold_pm


def get_p_in_dir(dir_path: str):
    """

    @param dir_path:
    @return:{"st":[[...sessiondata]..rounds]}
    """
    data_raw = {}
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            index = re.search(r's\d', file)
            if not index or not file.find('.json'):
                continue
            strategy_index = int(index.group()[-1])
            if strategy_index < len(labels):
                with open(os.path.join(root, file)) as res:
                    if data_raw.keys().__contains__(labels[strategy_index]):
                        data_raw[labels[strategy_index]].extend(json.load(res))
                    else:
                        data_raw[labels[strategy_index]] = json.load(res)
    data = {}
    for k, v in data_raw.items():
        data[k] = []
        for round in v:
            round_data = []
            for session in round:
                round_data.append(session["p"])
            data[k].append(round_data)
    return data


if __name__ == '__main__':
    data = {'random': [[1, 2, 3], [5, 6, 7], [9, 10, 12]]}
    print(get_data_rm_out_point(data))
    # get_data_in_dir('res/20_10_10_3519_2')
