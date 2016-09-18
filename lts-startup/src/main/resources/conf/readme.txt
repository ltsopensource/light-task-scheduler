
如果想要启动多个jobTracker，请复制一份 zoo。
譬如 我复制了一个zoo文件夹名称为 test1

那么文件组织方式为:
-conf
	-zoo
		-jobtracker.cfg
		-log4j.properties
	-test1
		-jobtracker.cfg
		-log4j.properties

那么执行的时候：

sh jobtracker.sh zoo {start|restart|stop}

sh jobtracker.sh test1 {start|restart|stop}
