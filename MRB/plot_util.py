1  # !/usr/bin/env python
2  # -*- coding: utf-8 -*-
3  # @Time  : 19:50
4  # @File  : plot_util.py
5  # @Author: Ch
6  # @Date  : 2020/3/2
import os

import matplotlib.pyplot as plt


def draw_variance_p(data: dict, save_path="out/img/default"):
    """
    绘制比较图，横坐标为读写器会话数0-20
    @param data: 数据
    @param save_path: 图片保存路径
    @return:
    """
    for k, v in data.items():
        plt.plot(v, label=k)
    plt.legend()
    plt.ylabel("p")
    plt.xlim(xmin=2)
    plt.xlabel("Number of reader sessions")
    plt.xticks(range(0, 20, 2))
    plt.savefig(save_path)
    plt.show()


def draw_plot(data: dict, y_label: str, save_path="out/img/default"):
    """
    绘图，横坐标为读写器会话数0-20
    @param y_label: y轴标注
    @param data: 数据{'xlabel'：[],}
    @param save_path: 图片保存路径
    @return:
    """
    slash_index = save_path.rfind('/')
    if slash_index != -1:
        dir = save_path[:save_path.rfind('/')]
    else:
        dir = ''
    if not os.path.isdir(dir):
        os.makedirs(dir)
    for k, v in data.items():
        plt.plot(v[0], label=k)
    plt.legend()
    plt.ylabel(y_label)
    plt.xlim(xmin=2)
    plt.xlabel("Number of reader sessions")
    plt.xticks(range(0, 20, 2))
    plt.savefig(save_path,dpi=200, bbox_inches='tight')
    plt.show()
