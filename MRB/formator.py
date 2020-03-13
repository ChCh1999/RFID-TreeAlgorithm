1  # !/usr/bin/env python
2  # -*- coding: utf-8 -*-
3  # @Time  : 21:46
4  # @File  : formator.py
5  # @Author: Ch
6  # @Date  : 2020/2/24

import seaborn as sns

from data_util import *
from plot_util import *

accurate = 0.3519999


def formator_slot(raw_data: list, save_path='out/img/slot'):
    """
    比较沉默方式使用的时隙数
    @param save_path:结果图片存储路径
    @param raw_data: [随机沉默[{"slot":},]，唯一碰撞集沉默，最小唯一碰撞集沉默，精确沉默]
    """
    labels = ["random", "CBM", "CBM_min", "accurate"]
    for strategy_data in raw_data:
        slot_record = [session["slot"] for session in strategy_data]
        for index in range(1, len(slot_record)):
            slot_record[index] += slot_record[index - 1]
        plt.plot(slot_record, label=labels[raw_data.index(strategy_data)])
    plt.legend()
    plt.ylabel("average cumulative number of slots used by MBR")
    plt.xlim(xmin=2)
    plt.xlabel("Number of reader sessions")
    plt.xticks(range(0, 20, 2))
    plt.savefig(save_path)
    plt.show()


def formator_p(raw_data: list, accurate_p, save_path='out/img/pm'):
    """
    比较沉默方式使用的时隙数
    @param save_path:结果图片存储路径
    @param accurate_p: 准确错误概率p
    @param raw_data: [随机沉默，唯一碰撞集沉默，最小唯一碰撞集沉默，精确沉默]
    """

    labels = ["random", "CBM", "CBM_min", "accurate"]
    data = {}
    for strategy_data in raw_data:
        p_record = [session["p"] for session in strategy_data]
        for index in range(len(p_record)):
            p_record[index] = abs(p_record[index] - accurate_p)
        data[labels[raw_data.index(strategy_data)]] = p_record
    draw_variance_p(data=data, save_path=save_path)


def formator_pm(raw_data: list, save_path='out/img/pm'):
    """
    比较沉默方式使用的时隙数
    @param raw_data: [随机沉默，唯一碰撞集沉默，最小唯一碰撞集沉默，精确沉默]
    @param save_path:结果图片存储路径
    """
    labels = ["random", "CBM", "CBM_min", "accurate"]
    for strategy_data in raw_data:
        pm_record = [session["pm"] for session in strategy_data]
        plt.plot(pm_record, label=labels[raw_data.index(strategy_data)])
    plt.legend()
    plt.ylabel("pm")
    plt.xlim(xmin=2)
    plt.xlabel("Number of reader sessions")
    plt.xticks(range(0, 20, 2))
    plt.savefig(save_path)
    plt.show()


def get_data_avg_from_file(input_path):
    with open(input_path)as file:
        raw_data = json.load(file)
    data_count = len(raw_data)
    res = []
    for session in range(len(raw_data[0])):
        res.append({})
        for k, v in raw_data[0][session].items():
            res[session][k] = 0
    for data in raw_data:
        for session in range(len(data)):
            for k, v in data[session].items():
                res[session][k] += v / data_count
    return res


def get_data(input_path, key):
    data_key = []
    with open(input_path)as file:
        raw_data = json.load(file)
        for round in range(len(raw_data)):
            data_key.append([])
            for session in raw_data[round]:
                data_key[round].append(session[key])
    return data_key


def draw_plots():
    print()


def data_distribution(data_p: dir, dir_path: str):
    for k, v in data_p.items():
        data_1 = np.reshape(v, -1)
        sns.distplot(data_1, hist=True, kde=True, rug=True)  # 前两个默认就是True,rug是在最下方显示出频率情况，默认为False
        # bins=20 表示等分为20份的效果，同样有label等等参数
        sns.kdeplot(data_1, shade=False, color='r')  # shade表示线下颜色为阴影,color表示颜色是红色
        # sns.rugplot(data_1)  # 在下方画出频率情况
        save_path = dir_path + "/out"
        if not os.path.exists(save_path):
            os.mkdir(save_path)
        plt.savefig(dir_path + "/out/distribution_" + k)
        plt.show()


def MBR_formator(dir_path: str):
    labels = ["random", "CBM", "CBM_min", "accurate", "random_a", "CBM_r", "CBM_min_r"]
    data = get_data_in_dir(dir_path)
    data_p_r = data["p"]
    data_p = sub_a_num(data_p_r, accurate)

    data_distribution(data_p, dir_path)

    data_pm = data['pm']
    data_slot_r = data["slot"]
    data_slot = get_sum(data_slot_r)

    data_p_mean = get_data_avg(data_p)
    data_p_mean_abs = get_abs(data_p_mean)
    data_pm_mean = get_data_avg(data_pm)
    data_slot_mean = get_data_avg(data_slot)

    draw_plot(data_p_mean_abs, 'variance in estimate p', dir_path + "/out/p/p_mean")
    draw_plot(data_pm_mean, 'pm', dir_path + "/out/pm/pm_mean")
    draw_plot(data_slot_mean, 'average cumulative number of slots used by MBR', dir_path + "/out/slot/slot_mean")

    data_p_mean_abs_softer = get_convolved(data_p_mean_abs, 5)
    draw_plot(data_p_mean_abs_softer, 'variance in estimate p', dir_path + "/out/p/p_softer")
    data_p_mean_bias = get_bias(data_p_mean)
    data_p_mean_bias_2 = get_bias(data_p_mean, 4)
    data_pm_mean_bias = get_bias(data_pm_mean)
    data_slot_mean_bias = get_bias(get_data_avg(data_slot))

    draw_plot(data_p_mean_bias, 'variance in estimate p compare with random', dir_path + "/out/p/p_mean_bias")
    draw_plot(data_p_mean_bias_2, 'variance in estimate p compare with random_a', dir_path + "/out/p/p_mean_bias_2")
    draw_plot(data_pm_mean_bias, 'pm compare with random', dir_path + "/out/pm/pm_mean_bias")
    draw_plot(data_slot_mean_bias, 'slots used by MBR compare with random',
              dir_path + "/out/slot/slot_mean_bias")

    data_p_rm = get_data_rm_out_point(data_p)
    data_p_rm_abs = get_abs(data_p_rm)
    data_pm_rm = get_data_rm_out_point(data_pm)
    data_slot_rm = get_data_rm_out_point(data_slot)

    draw_plot(data_p_rm_abs, 'variance in estimate p', dir_path + "/out/p/p_rm")
    draw_plot(data_pm_rm, 'pm', dir_path + "/out/pm/pm_rm")
    draw_plot(data_slot_rm, 'average cumulative number of slots used by MBR', dir_path + "/out/slot/slot_rm")

    data_p_rm_bias = get_bias(data_p_rm)
    data_p_rm_bias_2 = get_bias(data_p_rm, 4)
    data_pm_rm_bias = get_bias(data_pm_rm)
    data_slot_rm_bias = get_bias(get_data_avg(data_slot))

    draw_plot(data_p_rm_bias, 'variance in estimate p compare with random', dir_path + "/out/p/p_rm_bias")
    draw_plot(data_p_rm_bias_2, 'variance in estimate p compare with random_a', dir_path + "/out/p/p_rm_bias_2")
    draw_plot(data_pm_rm_bias, 'pm compare with random', dir_path + "/out/pm/pm_rm_bias")
    draw_plot(data_slot_rm_bias, 'slots used by MBR compare with random',
              dir_path + "/out/slot/slot_rm_bias")

    # 中位数
    # data_p_mid = get_data_mid(data_p)
    # data_p_mid_abs = get_abs(data_p_mid)
    # data_pm_mid = get_data_mid(data_pm)
    # data_slot_mid = get_data_mid(data_slot)
    #
    # draw_plot(data_p_mid_abs, 'variance in estimate p', dir_path + "/out/p_mid")
    # draw_plot(data_pm_mid, 'pm', dir_path + "/out/pm_mid")
    # draw_plot(data_slot_mid, 'average cumulative number of slots used by MBR', dir_path + "/out/slot_mid")
    #
    # data_p_mid_bias = get_bias(data_p_mid)
    # data_pm_mid_bias = get_bias(data_pm_mid)
    # data_slot_mid_bias = get_bias(get_data_avg(data_slot_r))
    #
    # draw_plot(data_p_mid_bias, 'variance in estimate p compare with random', dir_path + "/out/p_mid_bias")
    # draw_plot(data_pm_mid_bias, 'pm compare with random', dir_path + "/out/pm_mid_bias")
    # draw_plot(data_slot_mid_bias, 'average cumulative number of slots used by MBR compare with random',
    #           dir_path + "/out/slot_mid_bias")


def p_scatter(dir_path: str):
    labels = ["random", "CBM", "CBM_min", "accurate"]
    path_0 = dir_path + '/s0_t80_tag1000.json'
    path_1 = dir_path + '/s1_t80_tag1000.json'
    path_2 = dir_path + '/s2_t80_tag1000.json'
    path_3 = dir_path + '/s3_t80_tag1000.json'
    datas_p_r = {
        labels[0]: get_data(path_0, 'p'),
        labels[1]: get_data(path_1, 'p'),
        labels[2]: get_data(path_2, 'p'),
        labels[3]: get_data(path_3, 'p')
    }
    datas_p = sub_a_num(datas_p_r, accurate)
    for k, v in datas_p.items():
        y_labels = [list(range(len(v[0])))] * len(v)
        plt.scatter(y_labels, v)
        plt.show()


if __name__ == '__main__':
    # variance in estimate p
    accurate = 1 - (1 - 0.2) * (1 - 0.1) * (1 - 0.1)
    # MBR_formator('res/20_10_10_3519_1000')
    # MBR_formator('res/20_10_10_3519_1000_2')
    # MBR_formator('res/20_10_10_3519_1000_3')

    MBR_formator('res/20_10_10_1000/0312_1')

    # MBR_formator('res/20_10_10_3519_100')
    # MBR_formator('res/20_10_10_3519_1000')
    # data_distribution('res/20_10_10_3519_1000')
    # MBR_formator('res/20_10_10_3519_1')