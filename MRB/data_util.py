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


def get_bias(data: dict,index=0):
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
        res[k] = list(v)
        for round_data in res[k]:
            for index in range(len(round_data)):
                round_data[index] = abs(round_data[index])
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


def get_data_rm_out_point(data: dict):
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
        Q3 = np.percentile(v, 75, axis=0)
        Q1 = np.percentile(v, 25, axis=0)
        IQR = np.subtract(Q3, Q1)
        IQR = np.multiply(IQR, 1.5)
        max_limit = np.add(mid, IQR)
        min_limit = np.subtract(mid, IQR)

        for round in v:
            for session_index in range(len(round)):
                record = round[session_index]
                if min_limit[session_index] <= record <= max_limit[session_index]:
                    datalist[session_index] += record
                    count[session_index] += 1

        for index in range(len(datalist)):
            datalist[index] = datalist[index] / count[index]
        res[k].append(datalist)
    # TODO:完成IQR计算
    return res


def get_data_in_dir(dir_path: str):
    data_raw = {}
    # labels = ["random", "CBM", "CBM_min", "accurate", "random_a", 'CBM_r', 'CBM_min_r']
    labels = ["random", "CBM", "CBM_min", "accurate", "random_a"]
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            index = re.search(r's\d', file)
            if not index:
                continue
            strategy_index = int(index.group()[-1])
            if strategy_index < len(labels):
                with open(os.path.join(root, file)) as res:
                    if data_raw.keys().__contains__(labels[strategy_index]):
                        data_raw[labels[strategy_index]].append(json.load(res))
                    data_raw[labels[strategy_index]] = json.load(res)
    data_keys = ['p', 'pm', 'slot']
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


if __name__ == '__main__':
    data = {'random': [[1, 2, 3], [5, 6, 7], [9, 10, 12]]}
    print(get_data_rm_out_point(data))
    # get_data_in_dir('res/20_10_10_3519_2')
