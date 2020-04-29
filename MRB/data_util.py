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


def get_sum(data: dict):
    """
    将数据转换为递增数据
    @param data:
    @return:
    """
    res = {}
    for k, v in data.items():
        res[k] = v
        for round_data in res[k]:
            for index in range(1, len(round_data)):
                round_data[index] += round_data[index - 1]
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
    # labels = ["random", "CBM", "CBM_min", "accurate", "random_a", 'CBM_r', 'CBM_min_r']
    labels = ["random", "CBM", "CBM_min", "accurate", "random_a"]
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


def get_p_in_dir(dir_path: str):
    """

    @param dir_path:
    @return:{"st":[[...sessiondata]..rounds]}
    """
    data_raw = {}
    # labels = ["random", "CBM", "CBM_min", "accurate", "random_a", 'CBM_r', 'CBM_min_r']
    labels = ["random", "CBM", "CBM_min", "accurate", "random_a"]
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
