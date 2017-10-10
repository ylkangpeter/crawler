# -*- coding: utf-8 -*-
from bs4 import BeautifulSoup
import urllib2
import urllib
import contextlib
import logging
import sys
import os
import time
import json
import io
import hashlib
import socket

# 30s timeout, be careful
socket.setdefaulttimeout(30)

# configs
LOG_FILENAME = 'szhome.log'
BASE_DIR = './house/'
SLEEP_TIME = 5

# init lof config
logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(filename)s[line:%(lineno)d] %(levelname)s %(message)s',
                    datefmt='%a, %d %b %Y %H:%M:%S',
                    filename=LOG_FILENAME);

root_url = "http://anju.szhome.com";
url_page = 'http://anju.szhome.com/project.html?prot=2&page=';
detail_url = "project/housedetail";
output_file = io.open("./result.json", 'w', encoding='utf8');
img_file = io.open("./result_img.txt", 'w', encoding='utf8');
img_dir = "img";

if not os.path.exists(img_dir):
    os.makedirs(img_dir)

result = [];


def parseDetailPage(html, message):
    message["detail_name"] = html.find("div", {"class": ["left", "cloum1left"]}).find("h1").text.strip();
    message["detail_price"] = html.find("dl", {"class": ["topinfo", "mb20"]}).find("dt").text.strip();
    message["detail_open_time"] = html.find("dl", {"class": ["topinfo", "mb20"]}).find("dd").text.strip();
    message["detail_keywords"] = html.find("div", {"class": ["keywords"]}).text.strip();
    message["detail_huxing"] = html.find("dl", {"class": ["midinfo"]}).text.strip();
    message["detail_address"] = html.find("p", class_="fix mb15").text.strip();

    pic_urls = html.find_all("a", class_="item");

    for i in pic_urls:
        album = i.find("p", class_="f15").text;
        if "(" in album:
            album = album[0:album.index("(")];
        message[album + "_url"] = root_url + i["href"];
        message[album] = []
        parsePictures(message, album, album + "_url");


def visitPage(url):
    logging.info("entering url: %s" % url);
    for _ in range(3):
        try:
            print "entering url: %s" % url;
            req = urllib2.Request(url);
            with contextlib.closing(urllib2.urlopen(req)) as response:
                the_page = response.read();
            return BeautifulSoup(the_page, 'html.parser', from_encoding="uft-8");
        except Exception:
            print "retry %s" % url;


def parsePictures(message, key, rootKey):
    # visit detail pages
    print message[rootKey]
    html = visitPage(message[rootKey])
    list = html.find("ul", class_="ad-thumb-list").find_all("li");
    for ll in list:
        thumb_img = ll.find("a")["href"];
        raw_img = ll.find("img")["src"];

        thumb_id = genMd5(thumb_img);
        raw_id = genMd5(raw_img)

        arr = [thumb_img, thumb_id, raw_img, raw_id];
        img_file.write(thumb_id + "\t" + thumb_img + "\n");
        img_file.write(raw_id + "\t" + raw_img + "\n");

        downloadImg(raw_img, img_dir + "/" + raw_id);
        downloadImg(thumb_img, img_dir + "/" + thumb_id);
        message[key].append(arr);


def downloadImg(url, fn):
    if url.startswith("http"):
        if not os.path.exists(fn):
            print "download img: %s %s" % (fn, url);
            for _ in range(3):
                try:
                    urllib.urlretrieve(url, fn);
                    break;
                except Exception:
                    print "retry %s" % url;

def genMd5(message):
    dd = hashlib.md5();
    dd.update(message);
    return dd.hexdigest();


def parseListPage(soup):
    # json message for storing house info

    # iterate list
    for link in soup.find_all("div", {"class": 'lpinfo'}):
        message = {};
        urls = link.find_all("a");

        # names = link.find_all("a");
        src = urls[0].find("img").get("src");
        message['cover_detail_src'] = root_url + link.find("div", {"class": "mianbox"}).find("a")[
            "href"];
        message['cover_xq_img'] = src;

        message['cover_xq_img_id'] = genMd5(src);
        img_file.write(genMd5(src) + "\t" + src + "\n");
        downloadImg(src, img_dir + "/" + genMd5(src));

        message['cover_xq_status'] = urls[0].find("p").text;
        message['cover_xq_name'] = urls[1].text;

        address = link.find("div", {"class": 'address'}).find_all("p");
        message['cover_district'] = address[0].text;
        message['cover_total_rooms'] = address[1].text;
        message['cover_decoration'] = address[2].text;

        price = link.find("div", {"class": 'price'}).text;
        message['cover_price'] = price;

        # visit detail pages
        html = visitPage(message['cover_detail_src'])
        parseDetailPage(html, message);

        room_no = urls[1]["href"].split("/")[-1][:-5];
        message["room_no"] = room_no;
        message["room_more_detail_url"] = root_url + "/" + detail_url + "/" + urls[1]["href"].split("/")[-1];

        parseMoreDetailPage(message, message["room_more_detail_url"]);
        result.append(message);
        logging.info(message);
        output_file.write(json.dumps(message).decode("unicode-escape") + ",\n");
        output_file.flush();

    return 1;


def parseMoreDetailPage(message, url):
    # visit detail pages
    html = visitPage(url);
    left_info = html.find("div", class_="left infoLeft");

    tmp_msg = {};

    tmp = left_info.find_all("li");
    infos = {};
    infos["price"] = tmp[0].text;
    infos["main_room_type"] = tmp[2].text;
    infos["developer"] = tmp[3].text;
    infos["wuye_type"] = tmp[4].text;
    infos["address"] = tmp[5].text;
    infos["keywords"] = tmp[6].text;
    infos["open_time"] = tmp[7].text;
    infos["area"] = tmp[8].text;
    infos["deliver_date"] = tmp[9].text;
    infos["building_area"] = tmp[10].text;
    infos["wuye_company"] = tmp[11].text;
    infos["total_rooms"] = tmp[12].text;
    infos["wuye_fee"] = tmp[13].text;
    infos["parking_lot"] = tmp[14].text;
    infos["rongjilv"] = tmp[15].text;
    infos["lvhualv"] = tmp[16].text;
    infos["yushouzheng"] = tmp[17].text;

    tmp_msg["details"] = infos;

    intro = html.find("div", class_="right infoRight").find("p").text.strip();
    tmp_msg["intro"] = intro;
    environment = html.find("div", class_="f-yh f14 bg-white infoWrap fix").find_all("div");
    tmp_msg["traffic"] = environment[1].text.strip();
    tmp_msg["others"] = environment[2].text.strip();
    message["more_details"] = tmp_msg;


def doooooooIt():
    global url_page, current_no;
    output_file.write(unicode("["));
    for i in range(1, 4):
        print "===========page %d ===========" % i;
        url = url_page + `i`;
        html = visitPage(url)
        parseListPage(html)
    output_file.write(unicode("]"));


doooooooIt()
