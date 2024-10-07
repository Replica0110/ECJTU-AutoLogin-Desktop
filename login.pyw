import subprocess

import requests
from win10toast import ToastNotifier


def login(student_id, password, isp):
    url = ('http://172.16.2.100:801/eportal/?c=ACSetting&a=Login&protocol=http:&hostname=172.16.2.100&'
           'iTermType=1&wlanacip=null&wlanacname=null&mac=00-00-00-00-00-00&&enAdvert=0&queryACIP=0&loginMethod=1')
    data = {
        "DDDDD": f',0,{student_id}@{isp}',
        "upass": password,
    }

    headers = {
        "User-Agent": ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                       "Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.27")
    }

    try:
        response = requests.post(url, data=data, headers=headers, timeout=2)
        return 1 if response.status_code == 200 else 0
    except requests.exceptions.ConnectionError:
        return 0


def check_network_status():
    """
    检查网络状态。

    :return: 网络不通返回True，否则返回False
    """
    result = subprocess.call('ping www.baidu.com -n 1', shell=True, stdin=subprocess.PIPE,
                             stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return bool(result)


def notify(title, message, duration=2):
    toaster = ToastNotifier()
    toaster.show_toast(title, message, duration=duration, threaded=False)


def main(student_id, password, isp):
    if check_network_status():
        notify("网络状态", "网络未连接，尝试连接网络")
        login_result = login(student_id, password, isp)
        if login_result == 0:
            notify("网络状态", "网络连接失败")
        else:
            notify("网络状态", "网络已连接")
    else:
        notify("网络状态", "网络已连接")


if __name__ == "__main__":
    student_id = "12346789" // 学号
    password = "123456789" // 密码
    isp = "cmcc" // 中国移动："cmcc"  联通："telecom"  电信："unicom"
    main(student_id, password, isp)
