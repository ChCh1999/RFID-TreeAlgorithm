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
labels = Config.labels


class NumpyEncoder(json.JSONEncoder):
    """ Special json encoder for numpy types """

    def default(self, obj):
        if isinstance(obj, (np.int_, np.intc, np.intp, np.int8,
                            np.int16, np.int32, np.int64, np.uint8,
                            np.uint16, np.uint32, np.uint64)):
            return int(obj)
        elif isinstance(obj, (np.float_, np.float16, np.float32,
                              np.float64)):
            return float(obj)
        elif isinstance(obj, (np.ndarray,)):  #### This is the fix
            return obj.tolist()
        return json.JSONEncoder.default(self, obj)


def formator_slot(raw_data: list, save_path='out/img/slot', do_sum=True):
    """
    比较沉默方式使用的时隙数
    @param save_path:结果图片存储路径
    @param raw_data: [随机沉默[{"slot":},]，唯一碰撞集沉默，最小唯一碰撞集沉默，精确沉默]
    """
    for strategy_data in raw_data:
        slot_record = [session["slot"] for session in strategy_data]
        if do_sum:
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
    raw_datas = get_data_in_dir(dir_path, ['p', 'pm', 'pm_t', 'slot', "CBMCount", "sameCBMCount"])

    # 获取p
    data_p_raw = raw_datas["p"]
    data_p = sub_a_num(data_p_raw, accurate)

    # pm
    data_pm = raw_datas['pm']

    # pm_t
    data_pm_t = raw_datas['pm_t']

    # slot
    data_slot_raw = raw_datas["slot"]
    data_slot = get_sum(data_slot_raw)

    # p的分布情况
    # data_distribution(data_p, dir_path + "/data", "variance in estimate p", 'session count')

    # 取均值
    data_p_mean = get_data_avg(data_p)
    data_p_mean_abs = get_abs(data_p_mean)
    data_pm_mean = get_data_avg(data_pm)
    data_pm_log = get_log(data_pm_mean)
    data_pm_t_mean = get_data_avg(data_pm_t)
    data_slot_mean = get_data_avg(data_slot)
    data_slot_raw_mean = get_data_avg(data_slot_raw)

    # data_p_mean = get_data_avg_from_dict_of_list(data_p)
    # data_p_mean_abs = get_abs(data_p_mean)
    # data_pm_mean = get_data_avg_from_dict_of_list(data_pm)
    # data_pm_log = get_log(data_pm_mean)
    # data_pm_t_mean = get_data_avg_from_dict_of_list(data_pm_t)
    # data_slot_mean = get_data_avg_from_dict_of_list(data_slot)
    # data_slot_raw_mean = get_data_avg_from_dict_of_list(data_slot_raw)



    # 绘制均值图
    draw_plot(data_p_mean, 'variance in estimate p', dir_path + "/out/p/p_mean_raw")
    draw_plot(data_p_mean_abs, 'variance in estimate p', dir_path + "/out/p/p_mean_abs")
    draw_plot(data_pm_mean, 'pm', dir_path + "/out/pm/pm_mean")
    draw_plot(data_pm_log, 'log10(pm)', dir_path + "/out/pm/pm_mean_log")
    draw_plot(data_pm_t_mean, 'pm_t', dir_path + "/out/pm_t/pm_t_mean")
    draw_plot(data_slot_mean, 'average cumulative number of slots used by MBR', dir_path + "/out/slot/slot_mean")
    with open(dir_path + "/out/slot/data_slot_mean.json", 'w') as wf:
        json.dump(data_slot_raw_mean, wf, cls=NumpyEncoder, indent=4)
    draw_plot(data_slot_raw_mean, 'number of slots used by MBR in each session'
              , dir_path + "/out/slot/slot_mean_raw")

    data_p_mean_bias = get_bias(data_p_mean)
    data_p_mean_bias_2 = get_bias(data_p_mean, 4)
    data_pm_mean_bias = get_bias(data_pm_mean)
    data_pm_t_mean_bias = get_bias(data_pm_t_mean)
    data_slot_mean_bias = get_bias(get_data_avg(data_slot))
    data_slot_raw_mean_bias = get_bias(data_slot_raw_mean)

    draw_plot(data_p_mean_bias, 'variance in estimate p compare with random', dir_path + "/out/p/p_mean_bias")
    draw_plot(data_p_mean_bias_2, 'variance in estimate p compare with random_a', dir_path + "/out/p/p_mean_bias_2")
    draw_plot(data_pm_mean_bias, 'pm compare with random', dir_path + "/out/pm/pm_mean_bias")
    draw_plot(data_pm_t_mean_bias, 'pm_t compare with random', dir_path + "/out/pm_t/pm_t_mean_bias")
    draw_plot(data_slot_mean_bias, 'slots used by MBR compare with random',
              dir_path + "/out/slot/slot_mean_bias")
    draw_plot(data_slot_raw_mean_bias, 'slots used by MBR in each session compare with random',
              dir_path + "/out/slot/slot_mean_raw_bias")

    # IQR去除离群点
    # data_p_rm = get_data_rm_out_point(data_p, 1.5)
    #
    # data_distribution(data_p_rm, dir_path + "/data/rm", "variance in estimate p", 'session count')
    # data_p_rm_abs = get_abs(data_p_rm)
    # data_pm_rm = get_data_rm_out_point(data_pm)
    # data_pm_t_rm = get_data_rm_out_point(data_pm_t)
    # data_slot_rm = get_data_rm_out_point(data_slot)
    #
    # draw_plot(data_p_rm, 'variance in estimate p', dir_path + "/out/p/p_rm_raw")
    # draw_plot(data_p_rm_abs, 'variance in estimate p', dir_path + "/out/p/p_rm_abs")
    # draw_plot(data_pm_rm, 'pm', dir_path + "/out/pm/pm_rm")
    # draw_plot(data_pm_t_rm, 'pm_t', dir_path + "/out/pm_t/pm_t_rm")
    # draw_plot(data_slot_rm, 'average cumulative number of slots used by MBR', dir_path + "/out/slot/slot_rm")
    #
    # data_p_rm_bias = get_bias(data_p_rm)
    # data_p_rm_bias_2 = get_bias(data_p_rm, 4)
    # data_pm_rm_bias = get_bias(data_pm_rm)
    # data_pm_t_rm_bias = get_bias(data_pm_t_rm)
    # data_slot_rm_bias = get_bias(get_data_avg(data_slot))
    #
    # draw_plot(data_p_rm_bias, 'variance in estimate p compare with random', dir_path + "/out/p/p_rm_bias")
    # draw_plot(data_p_rm_bias_2, 'variance in estimate p compare with random_a', dir_path + "/out/p/p_rm_bias_2")
    # draw_plot(data_pm_rm_bias, 'pm compare with random', dir_path + "/out/pm/pm_rm_bias")
    # draw_plot(data_pm_t_rm_bias, 'pm_t compare with random', dir_path + "/out/pm_t/pm_t_rm_bias")
    # draw_plot(data_slot_rm_bias, 'slots used by MBR compare with random',
    #           dir_path + "/out/slot/slot_rm_bias")

    # # 中位数
    # data_p_mid = get_data_mid(data_p)
    # draw_plot(data_p_mid, 'variance in estimate p', dir_path + "/out/p/p_mid_raw")
    # data_p_mid_abs = get_abs(data_p_mid)
    #
    # draw_plot(data_p_mid_abs, 'variance in estimate p', dir_path + "/out/p/p_mid_abs")
    #
    # data_p_mid_bias = get_bias(data_p_mid)
    #
    # draw_plot(data_p_mid_bias, 'variance in estimate p compare with random', dir_path + "/out/p/p_mid_bias")

    data_CBMCount = raw_datas['CBMCount']
    data_CBMCount_mean = get_data_avg(data_CBMCount)
    draw_plot(data_CBMCount_mean, 'Count of CBM', dir_path + "/out/CBMCount/CBMCount_mean_raw")
    data_of_same_cbm_count = raw_datas['sameCBMCount']
    data_of_same_cbm_count_mean = get_data_avg(data_of_same_cbm_count)
    draw_plot(data_of_same_cbm_count_mean, 'Count of CBM that occur in last frame',
              dir_path + "/out/CBMOfSameCollisionBitsCount/CBMOfSameCollisionBitsCount_mean_raw")

    # slot与pm的关系
    # draw_plot_two_axis(data_pm_mean, "pm", data_slot_mean, "slot", dir_path + "/out/slotAndPm/slotAndPm_mean_raw")

    draw_plot_two_axis(data_slot_mean, "slot", data_pm_mean, "pm", dir_path + "/out/slotAndPm"
                                                                              "/pmAndSlot_mean_raw")
    data_slot_threshold, data_pm_threshold = get_slot_pm_threshold(data_slot_mean, data_pm_mean, 0.01)
    draw_plot_two_axis(data_pm_threshold, "pm", data_slot_threshold, "slot",
                       dir_path + "/out/slotAndPm/pmAndSlot_mean_raw_threshold")
    data_slot_threshold, data_pm_threshold = get_slot_pm_threshold(data_slot_mean, data_pm_mean, 0.001)
    draw_plot_two_axis(data_pm_threshold, "pm", data_slot_threshold, "slot",
                       dir_path + "/out/slotAndPm/pmAndSlot_mean_raw_threshold_001")
    # draw_plot_two_axis(data_slot_mean, "slot", data_pm_t_mean, "pm_t", dir_path +
    # "/out/slotAndPm/pm_tAndSlot_mean_raw")

def MBR_formator_of_pm_divided_input(dir_path: str, session_limit :int = -1):
    '''
    绘制除pm-slot外的各种图形，和MBR_formator_20的区别在于此函数用于处理以pm为实验中止条件的数据
    和MBR_formator_20在实现上的主要区别为用get_data_avg_from_dict_of_list代替了get_data_avg
    @author fwh
    @param dir_path: 数据路径，该目录下的子目录应为0.001,0.002,0.003....代表以pm=xxx为实验中止条件的数据,或者该目录可直接为0.001,0.002......
    @return:
    '''
    raw_datas = get_data_in_dir(dir_path, ['p', 'pm', 'pm_t', 'slot', "CBMCount", "sameCBMCount"])

    if session_limit != -1:
        raw_datas = remove_data_of_too_few_session(raw_datas, session_limit)

    # 获取p
    data_p_raw = raw_datas["p"]
    data_p = sub_a_num(data_p_raw, accurate)

    # pm
    data_pm = raw_datas['pm']

    # pm_t
    data_pm_t = raw_datas['pm_t']

    # slot
    data_slot_raw = raw_datas["slot"]
    data_slot = get_sum(data_slot_raw)

    # CBMCount
    data_CBMCount = raw_datas['CBMCount']

    # sameCBMCount
    data_of_same_cbm_count = raw_datas['sameCBMCount']

    # p的分布情况
    # data_distribution(data_p, dir_path + "/data", "variance in estimate p", 'session count')

    # 取均值
    # data_p_mean = get_data_avg(data_p)
    # data_p_mean_abs = get_abs(data_p_mean)
    # data_pm_mean = get_data_avg(data_pm)
    # data_pm_log = get_log(data_pm_mean)
    # data_pm_t_mean = get_data_avg(data_pm_t)
    # data_slot_mean = get_data_avg(data_slot)
    # data_slot_raw_mean = get_data_avg(data_slot_raw)

    data_p_mean = get_data_avg_from_dict_of_list_return_min_count_of_frame_data(data_p)
    data_p_mean_abs = get_abs(data_p_mean)
    data_pm_mean = get_data_avg_from_dict_of_list_return_min_count_of_frame_data(data_pm)
    data_pm_log = get_log(data_pm_mean)
    data_pm_t_mean = get_data_avg_from_dict_of_list_return_min_count_of_frame_data(data_pm_t)
    data_slot_mean = get_data_avg_from_dict_of_list_return_min_count_of_frame_data(data_slot)
    data_slot_raw_mean = get_data_avg_from_dict_of_list_return_min_count_of_frame_data(data_slot_raw)
    data_CBMCount_mean = get_data_avg_from_dict_of_list_return_min_count_of_frame_data(data_CBMCount)
    data_of_same_cbm_count_mean = get_data_avg_from_dict_of_list_return_min_count_of_frame_data(data_of_same_cbm_count)

    # 绘制均值图
    draw_plot(data_p_mean, 'variance in estimate p', dir_path + "/out/p/p_mean_raw")
    draw_plot(data_p_mean_abs, 'variance in estimate p', dir_path + "/out/p/p_mean_abs")
    draw_plot(data_pm_mean, 'pm', dir_path + "/out/pm/pm_mean")
    draw_plot(data_pm_log, 'log10(pm)', dir_path + "/out/pm/pm_mean_log")
    draw_plot(data_pm_t_mean, 'pm_t', dir_path + "/out/pm_t/pm_t_mean")
    draw_plot(data_slot_mean, 'average cumulative number of slots used by MBR', dir_path + "/out/slot/slot_mean")
    with open(dir_path + "/out/slot/data_slot_mean.json", 'w') as wf:
        json.dump(data_slot_raw_mean, wf, cls=NumpyEncoder, indent=4)
    draw_plot(data_slot_raw_mean, 'number of slots used by MBR in each session'
              , dir_path + "/out/slot/slot_mean_raw")

    data_p_mean_bias = get_bias(data_p_mean)
    data_p_mean_bias_2 = get_bias(data_p_mean, 4)
    data_pm_mean_bias = get_bias(data_pm_mean)
    data_pm_t_mean_bias = get_bias(data_pm_t_mean)
    data_slot_mean_bias = get_bias(data_slot_mean)
    data_slot_raw_mean_bias = get_bias(data_slot_raw_mean)


    draw_plot(data_p_mean_bias, 'variance in estimate p compare with random', dir_path + "/out/p/p_mean_bias")
    draw_plot(data_p_mean_bias_2, 'variance in estimate p compare with random_a', dir_path + "/out/p/p_mean_bias_2")
    draw_plot(data_pm_mean_bias, 'pm compare with random', dir_path + "/out/pm/pm_mean_bias")
    draw_plot(data_pm_t_mean_bias, 'pm_t compare with random', dir_path + "/out/pm_t/pm_t_mean_bias")
    draw_plot(data_slot_mean_bias, 'slots used by MBR compare with random',
              dir_path + "/out/slot/slot_mean_bias")
    draw_plot(data_slot_raw_mean_bias, 'slots used by MBR in each session compare with random',
              dir_path + "/out/slot/slot_mean_raw_bias")
    draw_plot(data_CBMCount_mean, 'Count of CBM', dir_path + "/out/CBMCount/CBMCount_mean_raw")
    draw_plot(data_of_same_cbm_count_mean, 'Count of CBM that occur in last frame',
              dir_path + "/out/CBMOfSameCollisionBitsCount/CBMOfSameCollisionBitsCount_mean_raw")

    # slot与pm的关系
    # draw_plot_two_axis(data_pm_mean, "pm", data_slot_mean, "slot", dir_path + "/out/slotAndPm/slotAndPm_mean_raw")

    # draw_plot_two_axis(data_slot_mean, "slot", data_pm_mean, "pm", dir_path + "/out/slotAndPm"
    #                                                                           "/pmAndSlot_mean_raw")
    # data_slot_threshold, data_pm_threshold = get_slot_pm_threshold(data_slot_mean, data_pm_mean, 0.01)
    # draw_plot_two_axis(data_pm_threshold, "pm", data_slot_threshold, "slot",
    #                    dir_path + "/out/slotAndPm/pmAndSlot_mean_raw_threshold")
    # data_slot_threshold, data_pm_threshold = get_slot_pm_threshold(data_slot_mean, data_pm_mean, 0.001)
    # draw_plot_two_axis(data_pm_threshold, "pm", data_slot_threshold, "slot",
    #                    dir_path + "/out/slotAndPm/pmAndSlot_mean_raw_threshold_001")
    # draw_plot_two_axis(data_slot_mean, "slot", data_pm_t_mean, "pm_t", dir_path +
    # "/out/slotAndPm/pm_tAndSlot_mean_raw")

def draw_slot_threshold_pm_2n(dir_path: str):
    data_slot_threshold, data_pm_threshold = get_slot_pm_threshold_2n(dir_path)
    draw_plot_two_axis(data_pm_threshold, "pm", data_slot_threshold, "slot",
                       dir_path + "/out/slotAndPm/pmAndSlot_mean_raw_threshold_001")


def draw_slot_threshold_pm(dir_path: str):
    data_slot_threshold, data_pm_threshold = get_slot_pm_threshold(dir_path)
    draw_plot_two_axis(data_pm_threshold, "pm", data_slot_threshold, "slot",
                       dir_path + "/out/slotAndPm/pmAndSlot_mean_raw_threshold_con")

def draw_dep_var_threshold_pm(dir_path: str, dep_var: str):
    data_slot_threshold, data_pm_threshold = get_dep_var_pm_threshold(dir_path, dep_var)
    draw_plot_two_axis(data_pm_threshold, "pm", data_slot_threshold, dep_var,
                       dir_path + "/out/pmAnd"+dep_var+"_mean_raw_threshold_con")


def p_session(dir_path: str, session=0, out_dir_path=""):
    """
    获取某个session的p分布
    @param dir_path:
    @param session:
    @return:
    """

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



def testFixedTagsMutilRound(dir_path: str):
    '''
    为了测试CBM_no的数据和龚建民学长的总相差一些slot所做的测试函数的数据处理
    bug解决后应该删除此函数
    @author fwh
    @param dir_path: 测试模拟数据
    @return:
    '''
    raw_datas = get_data_in_dir(dir_path, ['p', 'pm', 'pm_t', 'slot', "CBMCount", "sameCBMCount"])

    data_slot_raw = raw_datas["slot"]
    data_slot = get_sum(data_slot_raw)

    data_slot_mean = get_data_avg_from_dict_of_list_return_all_frame_data(data_slot)
    data_slot_mean_cbm_no = data_slot_mean["CBM_no"]

    data_slot_cbm_no = data_slot["CBM_no"]
    data_slot_cbm_no_total = []
    for v in data_slot_cbm_no:
        data_slot_cbm_no_total.append(v[-1])
    data_slot_cbm_no_total_avg = np.array(data_slot_cbm_no_total).mean(axis=0)

    f = open('res/test/result.txt', 'w')
    for v in range(len(data_slot_mean_cbm_no)):
        f.write("第"+str(v)+"个Frame消耗slot平均为"+str(data_slot_mean_cbm_no[v])+"个\n")
    f.write("\n")
    f.write("平均识别完成需要使用"+str(data_slot_cbm_no_total_avg)+"个slot\n")
    f.close()




    print("alu")





if __name__ == '__main__':
    # variance in estimate p
    accurate = 1 - (1 - 0.2) * (1 - 0.1) * (1 - 0.1)

    # MBR_formator_20('res/20_10_10_1000_10/0507')
    # draw_slot_threshold_pm("res/continuous/0623_1")
    # draw_slot_threshold_pm("res/continuous/0712");
    # draw_dep_var_threshold_pm("res/continuous/0710", "p")
    # draw_dep_var_threshold_pm("res/continuous/0710", "slot")
    # MBR_formator_of_pm_divided_input("res/continuous/0711",18)

    # draw_dep_var_threshold_pm("res/continuous/0710", "sameCBMCount")
    #draw_slot_threshold_pm("res/continuous/0619")

    testFixedTagsMutilRound("res/test")