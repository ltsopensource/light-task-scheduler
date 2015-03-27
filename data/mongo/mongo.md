


use job;
db.createCollection("JobPo");
db.addUser("lts", "lts");
db.auth("lts", "lts");
db.JobPo.ensureIndex({"jobId":1},{unique:true});
db.JobPo.ensureIndex({"taskTrackerNodeGroup":1, "taskId":1},{unique:true});
db.JobPo.ensureIndex({"taskTrackerIdentity":1});
db.JobPo.ensureIndex({"priority":1, "triggerTime":1});
db.JobPo.ensureIndex({"isRunning":1});
db.JobPo.ensureIndex({"taskTrackerNodeGroup":1, "isRunning":1, "triggerTime":1});


db.createCollection("JobLogPo");
db.addUser("lts", "lts");
db.auth("lts", "lts");

db.JobLogPo.ensureIndex({"jobId":1});
db.JobLogPo.ensureIndex({"submitNodeGroup":1, "taskId":1});
db.JobLogPo.ensureIndex({"taskTrackerIdentity":1});
db.JobLogPo.ensureIndex({"gmtCreate":1});
db.JobLogPo.ensureIndex({"priority":1});
db.JobLogPo.ensureIndex({"logType":1});
db.JobLogPo.ensureIndex({"timestamp":1});
db.JobLogPo.ensureIndex({"taskId":1});

db.createCollection("JobFeedbackPo");
db.addUser("lts", "lts");
db.auth("lts", "lts");

db.JobFeedbackPo.ensureIndex({"id":1});
db.JobFeedbackPo.ensureIndex({"gmtCreated":1});
