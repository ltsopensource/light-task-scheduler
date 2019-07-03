####1. Mission pause and resume function (emergency, short time) completed
The general idea is to create a new pause task table. When the task is paused by the user on the page, it will be moved to the pause table. When it is restored, it will be restored from the pause table to the executable table. Of course, this needs to consider the situation in which the task is being executed.
####2. Task real-time trigger function (emergency, short time)
This function is temporarily set to only have the Cron task to have this function. For real-time and scheduled tasks, you only need to modify the task execution time (if the scheduled task also needs this function, the internal only sets the task execution time and priority). As a rule, for Cron tasks, you need to add a new task internally to execute.
####3.LTS KV Storage Optimization:
The purpose of starting LTS's own KV storage is mainly to solve the dependence on the third-party KV storage engine, and various optimizations and customizations can be made for the usage scenarios of the LTS. The optimization section mainly includes:

* When the number of stored entries for each DataBlock is below a certain threshold (for example, 50%), you need to "garbage" the DataBlock, delete and organize some logically deleted data blocks.
* Index Index, currently only provides a memory implementation, but also needs to provide a B + tree implementation to solve the limitations of memory under large data volume. Index snapshot is currently used to resolve the issue of shortening the replay transaction log each time it is started.
* For the transaction log TxLog, there is currently no deletion policy. You need to add a policy: when the TxLog log file reaches a certain number, and the current number of kvs is 0, you can remove these TxLogs into the bak directory. The number of bak directories reaches a certain number or a certain period of time and then deletes.
* Can think about the problem of data compression

####4. Task monitoring and alarm (emergency, long time):
The monitoring aspect mainly focuses on several aspects:

* Monitoring and alarm for each node
* After a node of a group is fully hung, an alarm is required.
* For a certain machine's resources, such as memory, cpu, etc.
* Alarms for a TaskTracker consumer task failure rate of 80% (a threshold) and automatically isolates him.

* Monitoring and alarming of task execution indicators
* You can set an alarm for abnormal consumption of a certain task
* For the task consumption delay in the task queue, the alarm is issued when there are too many stacked
* Can be customized to receive a specific BizLog for alarm
* The alarm form can be SMS, email, etc.

####6.zookeeper client package complete
Mainly to remove the dependency on zkClient and curator
####7.LTS implementation of nio framework completion (to be optimized)
Mainly to remove the dependence on netty and mina
####8. Native support for task dependencies
####9.Processing the task business log
Can be divided by quantity
####10.TaskTracker increases the setting. When the memory of the node is insufficient or the cpu resources are insufficient, do not go to the pull task (complete)

####11. The monitoring center can be deployed multiple times, mainly used to collect some statistical information of the nodes, exposed by the registration center node, without manual setting of each node (completed)

####12. Add a job termination operation to the console, provide an interrupt interface (complete)

####13. The number of retries can be set at the task level (completed)
