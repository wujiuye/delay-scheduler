以下步骤总结了一次正常的Raft日志的复制流程:
* 1）客户端向Leader发送写请求。
* 2）Leader将写请求解析成操作指令追加到本地日志文件中。
* 3）Leader为每个Follower广播AppendEntriesRPC。
* 4）Follower通过一致性检查，选择从哪个位置开始追加Leader的日志条目。
* 5）一旦日志项提交成功，Leader就将该日志条目对应的指令应用（apply）到本地状态机，
并向客户端返回操作结果。
* 6）Leader后续通过AppendEntriesRPC将已经成功（在大多数节点上）提交的日志项告知Follower。
* 7）Follower收到提交的日志项之后，将其应用至本地状态机。
                                         

