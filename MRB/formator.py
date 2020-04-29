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


def data_distribution(data_p: dir, dir_path: str, x_label: str, y_label: str):
    for k, v in data_p.items():
        data_1 = np.reshape(v, -1)
        # 前两个默认就是True,rug是在最下方显示出频率情况，默认为False
        sns.distplot(data_1, hist=True, kde=True, rug=True)
        # 表示等分为20份的效果，同样有label等等参数
        # bins = 20
        # shade表示线下颜色为阴影,color表示颜色是红色
        sns.kdeplot(data_1, shade=False, color='r')
        # sns.rugplot(data_1)  # 在下方画出频率情况
        save_path = dir_path
        if not os.path.exists(save_path):
            os.makedirs(save_path)
        plt.xlabel(x_label)
        plt.ylabel(y_label)
        plt.savefig(save_path + "/distribution_" + k)
        plt.clf()
        # plt.show()


def MBR_formator_20(dir_path: str):
    labels = ["random", "CBM", "CBM_min", "accurate", "random_a", "CBM_r", "CBM_min_r"]
    data = get_data_in_dir(dir_path, ['p', 'pm', 'slot',"CBMCount","sameCBMCount"])
    data_p_r = data["p"]
    data_p = sub_a_num(data_p_r, accurate)

    data_distribution(data_p, dir_path + "/data", "variance in estimate p", 'session count')

    data_pm = data['pm']
    data_slot_r = data["slot"]
    data_slot = get_sum(data_slot_r)

    data_p_mean = get_data_avg(data_p)
    data_p_mean_abs = get_abs(data_p_mean)
    data_pm_mean = get_data_avg(data_pm)
    data_pm_log = get_log(data_pm_mean)
    data_slot_mean = get_data_avg(data_slot)

    draw_plot(data_p_mean, 'variance in estimate p', dir_path + "/out/p/p_mean_raw")
    draw_plot(data_p_mean_abs, 'variance in estimate p', dir_path + "/out/p/p_mean_abs")
    draw_plot(data_pm_mean, 'pm', dir_path + "/out/pm/pm_mean")
    draw_plot(data_pm_log, 'log10(pm)', dir_path + "/out/pm/pm_mean_log")
    draw_plot(data_slot_mean, 'average cumulative number of slots used by MBR', dir_path + "/out/slot/slot_mean")

    # data_p_mean_abs_softer = get_convolved(data_p_mean_abs, 5)
    # draw_plot(data_p_mean_abs_softer, 'variance in estimate p', dir_path + "/out/p/p_softer")
    data_p_mean_bias = get_bias(data_p_mean)
    data_p_mean_bias_2 = get_bias(data_p_mean, 4)
    data_pm_mean_bias = get_bias(data_pm_mean)
    data_slot_mean_bias = get_bias(get_data_avg(data_slot))

    draw_plot(data_p_mean_bias, 'variance in estimate p compare with random', dir_path + "/out/p/p_mean_bias")
    draw_plot(data_p_mean_bias_2, 'variance in estimate p compare with random_a', dir_path + "/out/p/p_mean_bias_2")
    draw_plot(data_pm_mean_bias, 'pm compare with random', dir_path + "/out/pm/pm_mean_bias")
    draw_plot(data_slot_mean_bias, 'slots used by MBR compare with random',
              dir_path + "/out/slot/slot_mean_bias")

    # IQR去除离群点
    data_p_rm = get_data_rm_out_point(data_p, 1.5)

    data_distribution(data_p_rm, dir_path + "/data/rm", "variance in estimate p", 'session count')
    data_p_rm_abs = get_abs(data_p_rm)
    data_pm_rm = get_data_rm_out_point(data_pm)
    data_slot_rm = get_data_rm_out_point(data_slot)

    draw_plot(data_p_rm, 'variance in estimate p', dir_path + "/out/p/p_rm_raw")
    draw_plot(data_p_rm_abs, 'variance in estimate p', dir_path + "/out/p/p_rm_abs")
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
    data_p_mid = get_data_mid(data_p)
    draw_plot(data_p_mid, 'variance in estimate p', dir_path + "/out/p/p_mid_raw")
    data_p_mid_abs = get_abs(data_p_mid)

    draw_plot(data_p_mid_abs, 'variance in estimate p', dir_path + "/out/p/p_mid_abs")

    data_p_mid_bias = get_bias(data_p_mid)

    draw_plot(data_p_mid_bias, 'variance in estimate p compare with random', dir_path + "/out/p/p_mid_bias")


    data_CBMCount = data['CBMCount']
    data_CBMCount_mean = get_data_avg(data_CBMCount)
    draw_plot(data_CBMCount_mean, 'Count of CBM', dir_path + "/out/CBMCount/CBMCount_mean_raw")
    # data_CBMOfSameCollisionBitsCount = data['CBMOfSameCollisionBitsCount']
    data_CBMOfSameCollisionBitsCount = data['sameCBMCount']
    data_CBMOfSameCollisionBitsCount_mean = get_data_avg(data_CBMOfSameCollisionBitsCount)
    draw_plot(data_CBMOfSameCollisionBitsCount_mean, 'Count of CBM that occur in last frame', dir_path + "/out/CBMOfSameCollisionBitsCount/CBMOfSameCollisionBitsCount_mean_raw")


def p_session(dir_path: str, session=0, out_dir_path=""):
    """
    获取某个session的p分布
    @param dir_path:
    @param session:
    @return:
    """
    labels = ["random", "CBM", "CBM_min", "accurate", "random_a", "CBM_r", "CBM_min_r"]
    data = get_data_in_dir(dir_path)
    data_p_r = data["p"]
    data_p = sub_a_num(data_p_r, accurate)
    data_p_2 = {}
    for strategy, v in data_p.items():
        data_p_2[strategy] = []
        for round in v:
            data_p_2[strategy].append(round[session])
    if out_dir_path == "":
        out_dir_path = dir_path
    data_distribution(data_p_2, out_dir_path + "/s" + str(session), "variance in estimate p", 'session count')


from sklearn.cluster import KMeans


def p_kmean(dirpath: str):
    data_p_raw = get_p_in_dir(dirpath)
    data_p_subaccurate = sub_a_num(data_p_raw, accurate)
    kmeans = KMeans(n_clusters=3)
    result = {}
    for s, data in data_p_subaccurate.items():
        result[s] = []
        ye = kmeans.fit(data)
        counts = [sum(ye.labels_ == i) for i in range(ye.n_clusters)]
        max_index = counts.index(max(counts))
        for index in range(len(data)):
            if ye.labels_[index] == max_index:
                result[s].append(data[index])
        result[s] = [np.mean(result[s], axis=0)]
    result = get_abs(data=result)
    draw_plot(data=result, y_label="p")
    return result


def MRB_best(dir_path: str, out_path: str):
    """
    数据估计结果的p分布
    @param dir_path:
    @param out_path:
    @return:
    """
    data_p_raw = get_p_in_dir(dir_path)
    data_p_sub_accurate = sub_a_num(data_p_raw, accurate)
    for k, v in data_p_sub_accurate.items():
        data = [i[-1] for i in v]
        session_count = [len(i) for i in v]
        sns.distplot(data, hist=True, kde=True, rug=True)

        # 绘制p计数
        sns.kdeplot(data, shade=False, color='r')
        # sns.rugplot(data_1)  # 在下方画出频率情况
        save_path = out_path
        if not os.path.exists(save_path):
            os.makedirs(save_path)
        plt.xlabel("estimation error of p")
        plt.ylabel("round count")
        plt.savefig(save_path + "/distribution_esti_p_" + k)
        # plt.show()
        plt.clf()



        # 绘制会话计数
        sns.distplot(session_count, hist=True, kde=True, rug=True)
        sns.kdeplot(session_count, shade=False, color='r')
        save_path = out_path
        if not os.path.exists(save_path):
            os.makedirs(save_path)
        plt.xlabel("count of session")
        plt.ylabel("round count")
        plt.savefig(save_path + "/distribution_session_count_" + k)
        # plt.show()
        plt.clf()
        print(k, np.mean(data), len(data), np.mean(session_count))


if __name__ == '__main__':
    # variance in estimate p
    accurate = 1 - (1 - 0.2) * (1 - 0.1) * (1 - 0.1)

    MBR_formator_20('res/20_10_10_1000_10/0429_1')
    # MBR_formator_20('res/20_10_10_1000/0414')
    # MBR_formator_20('res/20_10_10_1000/0414/0_6')
    # MBR_formator_20('res/20_10_10_1000/0414/7')
    # MBR_formator_20('res/20_10_10_1000/0414/8')
    # MBR_formator_20('res/20_10_10_1000/0414/9')
    # in_dir = 'res/20_10_10_1000/0414'
    # MRB_best(in_dir, in_dir + '/out')
    # in_dir = 'res/20_10_10_1000/0417-0.001'
    # MRB_best(in_dir, in_dir + '/out')
    # in_dir = 'res/20_10_10_1000/0417-0.001/0_5'
    # MRB_best(in_dir, in_dir + '/out')
    # in_dir = 'res/20_10_10_1000/0417-0.001/6_11'
    # MRB_best(in_dir, in_dir + '/out')
    # in_dir = 'res/20_10_10_1000/0417-0.001/12_17'
    # MRB_best(in_dir, in_dir + '/out')
    # in_dir = 'res/20_10_10_1000/0417-0.001/18_22'
    # MRB_best(in_dir, in_dir + '/out')
