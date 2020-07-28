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
    '''
    获取平均值，处理以session为实验中止条件的数据
    @param data:
    @return:
    '''
    res = {}
    for k, v in data.items():
        res[k] = []
        res[k].append(np.array(v).mean(axis=0))
    return res


def get_data_avg_from_dict_of_list_return_min_count_of_frame_data(data: dict):
    '''
    获取平均值，处理以pm为实验中止条件的数据。
    和get_data_avg的区别在于data的格式
    @author: fwh
    @param data: 字典。格式类似{"random":[[],[],...,[]]},其中最内层list长度不定
    @return: 字典，格式类似{"random":[]},列表中的每个元素是data中的最内层列表该位置的元素的平均值，所有list的长度均为data中最内层list长度的最小值
    '''
    res = {}
    ses_min = 100000;
    for (k, v) in data.items():
        res[k] = []
        # ses_min = 100000;
        for sim in v:
            if len(sim) < ses_min:
                ses_min = len(sim)
        for i in range(ses_min):
            res[k].append(0)
            for j in range(len(v)):
                res[k][i] = (res[k][i]*j + v[j][i]) / (j+1)

    for k in res.keys():
        a = res[k]
        res[k] = []
        res[k].append(a[:ses_min])
    return res


def get_round_arrive_time(data: list):
    '''
    获取一组不同的模拟数据中进行了大于等于n轮的模拟数据的个数
    @author fwh
    @param data: 数据，格式为[[],[],...,[]]，每个子列表是一组模拟数据，子列表长度可能不同
    @return:格式为[],其中第i个元素代表输入数据中进行了大于等于i轮后结束识别的模拟的个数
    '''
    res = []
    see_max = 0
    for m in data:
        if len(m) > see_max:
            for i in range(see_max, len(m)):
                res.append(0)
            see_max = len(m)
        res[len(m)-1] += 1
    for i in range(len(res)):
        for j in range(i):
            res[j] += res[i]
    return res


def get_data_avg_from_dict_of_list_return_all_frame_data(data: dict):
    '''
    获取平均值，用于将每一个Frame在不同的模拟中的数据取平均。
    和get_data_avg_from_dict_of_list_return_min_count_of_frame_data的区别在于，对于一些只在部分模拟中含有的Frame，
    此函数的返回结果会在该Frame的位置计算这些含有该Frame的模拟数据的平均。
    因此结果中可能会有部分数据只由很少的模拟平均而来，而且各个Frame的平均值对应的模拟次数会不相同。
    @author fwh
    @param data: 字典。格式类似{"random":[[],[],...,[]]},其中最内层list长度不定
    @return: 字典，格式类似{"random":[]},列表中的每个元素是data中的最内层列表该位置的元素的平均值，每一个list的长度均为data中对应的该list的内层list长度的最大值
    '''
    res = {}
    for (k, v) in data.items():
        res[k] = get_data_avg_from_list_of_list(v)
    return res


def get_data_avg_from_list_of_list(data: list):
    '''
    获取平均值，用于将每一个Frame在不同的模拟中的数据取平均
    对于第n个Frame，结果中该Frame的数据为输入数据中超过n个Frame的所有模拟在第n个Frame上的数据的平均值
    由于需要处理的数据可能随着实验进行再次发生变化，所以此函数需要直接将某一数据(slot、pm、p等)的数值列表作为输入
    @author fwh
    @param data: 列表。格式为[[],[],...,[]]，其中的每一个子列表代表一次模拟的数据，长度可能不同。所有的数据应该都具有同一意义（比如都是slot）
    @return:列表。格式为[],返回的列表长度与输入数据中最长的子列表相同，第i个元素代表输入数据中第i个Frame的数据的平均值
    '''
    res = []
    see_max = 0
    for m in data:
        if len(m) > see_max:
            for i in range(see_max,len(m)):
                res.append([])
            see_max = len(m)
        for i in range(len(m)):
            res[i].append(m[i])
    for i in range(len(res)):
        avg = np.array(res[i]).mean(axis=0)
        res[i] = avg
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


def get_dep_var_pm_threshold(dir_path: str, dep_var: str):
    """
    获取以阈值为标签的文件夹数据
    @param dir_path: 总文件夹目录，字目录均为阈值
    @param dep_var: 要获取的因变量的标识
    @return: 时隙数据、阈值数据
    """
    res_dep_var = {}
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
        data_temp = get_data_in_dir(os.path.join(dir_path, threshold_dir_list[i]), [dep_var])[dep_var]
        if dep_var == 'slot':
            data_temp = get_sum(data_temp)
        for k, v in data_temp.items():
            for index in range(len(v)):
                v[index] = [v[index][-1]]
        data_temp = get_data_avg(data_temp)
        for k in data_temp.keys():
            if k in res_dep_var.keys():
                res_dep_var[k][0].append(data_temp[k][0][-1])
            else:
                res_dep_var[k] = []
                res_dep_var[k].append([])
                res_dep_var[k][0].append(data_temp[k][0][-1])

    for k in labels:
        if k in res_dep_var.keys():
            res_threshold_pm[k] = []
            res_threshold_pm[k].append(list_threshold.copy())

    return res_dep_var, res_threshold_pm

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

def remove_data_of_too_few_session(data:dict, session_limit:int):
    res = {}
    for (k,v) in data.items():
        res_value = {}
        for (strategy_key,strategy_value) in v.items():
            res_strategy_value = []
            for session in strategy_value:
                if len(session) >= session_limit:
                    res_strategy_value.append(session)
            res_value[strategy_key] = res_strategy_value
        res[k] = res_value
    return res







if __name__ == '__main__':
    data = {'random': [[1, 2, 3], [5, 6, 7], [9, 10, 12]]}
    print(get_data_rm_out_point(data))
    # get_data_in_dir('res/20_10_10_3519_2')
