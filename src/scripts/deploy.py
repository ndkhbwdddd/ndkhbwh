#!/usr/bin/env python
# -*- coding: utf-8 -*-

from fabric.api import local, cd, sudo
from fabric.api import put, get
from fabric.api import env, task, run


path = '/home/xujinbo'

@task
def status():
    run("ps -ef |grep microchat.server-0.1.0.jar")
    run('screen -wipe', warn_only=True)

@task
def deploy():
	#get_hosts()
    put('E:/IM/microchat/microchat.server/target/microchat.server-0.1.0.jar',path,use_sudo=False) #put jmeter to remote:~
    put('E:/IM/microchat/microchat.model/target/microchat.model-0.0.1-SNAPSHOT.jar',path,use_sudo=False) #put jmeter to remote:~
    with cd(path):
    	run('rm -f /home/xujinbo/microchat.server-0.1.0/lib/microchat.server-0.1.0.jar')
    	run('cp /home/xujinbo/microchat.server-0.1.0.jar /home/xujinbo/microchat.server-0.1.0/lib/')
    	run('rm -f /home/xujinbo/microchat.server-0.1.0/lib/microchat.model-0.0.1-SNAPSHOT.jar')
    	run('cp /home/xujinbo/microchat.model-0.0.1-SNAPSHOT.jar /home/xujinbo/microchat.server-0.1.0/lib/')
    	stop()
        start()
        #sudo('tar xzf videome.serv-1-release.tar.gz')

@task
def uninstalled():
    with cd(path):
        print 'uninstalled begin'
        stop()
        #sudo('rm -rf videome.serv-1*')
        print 'uninstalled end'

@task
def stop():
	#run('/srv/hichat/hichat-access/bin/nmsg-server.sh stop')
    kill_process(str="microchat.server")

@task
def start():
    #sudo('screen -S videome.serv -d -m videome.serv-1/bin/netty.sh run; sleep 1')
    #sudo('screen -ls |grep videome.serv')
    run('/home/xujinbo/microchat.server-0.1.0/bin/microchat-server.sh start');
    run('screen -S microchat.server -d -m /home/xujinbo/microchat.server-0.1.0/bin/microchat-server.sh run; sleep 1 ')

@task
def get_hosts():
    env.user='xujinbo'
    env.password = 'Xjb111!!!'        
    env.hosts = ['192.168.0.3', ]       

@task
def kill_process(str):
    run("ps ax |grep %s |grep -v grep | awk '{print $1}' | xargs kill -9" % str, warn_only=True)