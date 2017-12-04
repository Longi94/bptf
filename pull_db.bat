adb shell "run-as com.tlongdev.bktf chmod 666 /data/data/com.tlongdev.bktf/databases/bptf.db"
adb exec-out run-as com.tlongdev.bktf cat databases/bptf.db > bptf.db
adb shell "run-as com.tlongdev.bktf chmod 600 /data/data/com.tlongdev.bktf/databases/bptf.db"
