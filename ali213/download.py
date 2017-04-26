# -*- coding: utf-8 -*-
from bs4 import BeautifulSoup
import urllib2
import urllib
import contextlib
import logging  
import sys  
import os
import time

#configs
LOG_FILENAME='ali213_download.log'
BASE_DIR='./gifs/'
SLEEP_TIME=5
html_filename="index.html"

#init lof config
logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(filename)s[line:%(lineno)d] %(levelname)s %(message)s',
                    datefmt='%a, %d %b %Y %H:%M:%S',
                    filename=LOG_FILENAME,
                    filemode='w');
url_page='http://www.ali213.net/news/bxgif/';

#latest file
files=os.listdir(BASE_DIR)
sorted_list = sorted(files, reverse=True)

latest_file="0"
if sorted_list is not None and len(sorted_list)>0:
  latest_file=sorted_list[0]
         
def download(pic_url,path):
  retry=0;
  while retry<3:
    urllib.urlretrieve(pic_url,path)
    #check if file is correct
    size=os.path.getsize(path)
    if size>150000:
      break;
    retry+=1;
    logging.error("wait %s second and retry connection issue & retry.",SLEEP_TIME);
    time.sleep(SLEEP_TIME);


def next_page(detail_page,li):
  #check there is next page
  next_page=detail_page.find(id="after_this_page");
  if next_page is None:
    return None;
  else:
    cur_page=li.split("/")[-1];
    end=len(li)-len(cur_page)
    return li[0:end]+next_page.get("href");  


def doIt(soup):
  #iterate list
  for link in soup.find_all('a'):
    li=str(link.get('href'));
    if li.startswith("http://www.ali213.net/news/html"):
      logging.info(li)
      # urllib.urlretrieve(imgUrl, os.path.basename(imgUrl));
      tmps=li.split('/');
      date=tmps[-2]+"_"+tmps[-1]+"/";
      path=BASE_DIR+date;
      print date,latest_file+"/"
      if date==latest_file+"/":
        logging.info("no updates anymore. quit download!")
        return None;

      if not os.path.exists(path):
        os.makedirs(path)
      

      f = open(path+html_filename, 'w')
      f.write("<html><body>")
      # visit detail pages
      while True:
        try:
          with contextlib.closing(urllib2.urlopen(li)) as rr:
            logging.info("enter: %s",li)
            re=rr.read();
            detail_page=BeautifulSoup(re,'html.parser')
            pic_urls=detail_page.findAll("img", { "alt" : "游侠网" })
            #logging.info(pic_urls)
            for url in pic_urls:
              pic_url=url.get('src');
              logging.info("downloading page:-----%s-----",pic_url)
              file_name=pic_url.split('/')[-1];
              print "<p><img src=\""+pic_url+"\"></p>"
              f.write("<p><img src=\""+pic_url+"\"></p>")
              # download(pic_url,path+file_name);

            li=next_page(detail_page,li);
            if li is None:
              break;

        except Exception,e:
          logging.error(e);
      f.write("</body></html>")
      f.close()
  return 1;

def doooooooIt():
  # todo download earlier gifs
  # 
  # while True:
    #enter list page
  global url_page
  tmp_page=url_page;
  req = urllib2.Request(tmp_page)
  with contextlib.closing(urllib2.urlopen(req)) as response:
    the_page = response.read();

  soup=BeautifulSoup(the_page,'html.parser');
  url_page=doIt(soup)
    # if page is none:
    #   break;

doooooooIt()