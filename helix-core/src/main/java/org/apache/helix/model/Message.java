package org.apache.helix.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.helix.HelixException;
import org.apache.helix.HelixProperty;
import org.apache.helix.InstanceType;
import org.apache.helix.PropertyKey;
import org.apache.helix.ZNRecord;
import org.apache.helix.PropertyKey.Builder;


/**
 * 
 */

public class Message extends HelixProperty
{
  public enum MessageType
  {
    STATE_TRANSITION,
    SCHEDULER_MSG,
    USER_DEFINE_MSG,
    CONTROLLER_MSG,
    TASK_REPLY,
    NO_OP,
    PARTICIPANT_ERROR_REPORT
  };

  public enum Attributes
  {
    MSG_ID,
    SRC_SESSION_ID,
    TGT_SESSION_ID,
    SRC_NAME,
    TGT_NAME,
    SRC_INSTANCE_TYPE,
    MSG_STATE,
    PARTITION_NAME,
    RESOURCE_NAME,
    FROM_STATE,
    TO_STATE,
    STATE_MODEL_DEF,
    CREATE_TIMESTAMP,
    READ_TIMESTAMP,
    EXECUTE_START_TIMESTAMP,
    MSG_TYPE,
    MSG_SUBTYPE,
    CORRELATION_ID,
    MESSAGE_RESULT,
    EXE_SESSION_ID,
    TIMEOUT,
    RETRY_COUNT,
    STATE_MODEL_FACTORY_NAME,
    BUCKET_SIZE,
    PARENT_MSG_ID,   // used for group message mode
    INNER_MESSAGE
  }

  public enum MessageState
  {
    NEW, 
    READ, // not used
    UNPROCESSABLE // get exception when create handler
  }

  public static final Comparator<Message> CREATE_TIME_COMPARATOR = new Comparator<Message>(){
    @Override
    public int compare(Message m1, Message m2)
    {
//      long t1 = m1.getCreateTimeStamp();
//      long t2 = m2.getCreateTimeStamp();
      return (int) (m1.getCreateTimeStamp() - m2.getCreateTimeStamp());
    }
  };
  
  // AtomicInteger _groupMsgCountDown = new AtomicInteger(1);
  
  public Message(MessageType type, String msgId)
  {
    this(type.toString(), msgId);
  }

  public Message(String type, String msgId)
  {
    super(new ZNRecord(msgId));
    _record.setSimpleField(Attributes.MSG_TYPE.toString(), type);
    setMsgId(msgId);
    setMsgState(MessageState.NEW);
    _record.setLongField(Attributes.CREATE_TIMESTAMP.toString(), new Date().getTime());
  }

  public Message(ZNRecord record)
  {
    super(record);
    if (getMsgState() == null)
    {
      setMsgState(MessageState.NEW);
    }
    if (getCreateTimeStamp() == 0)
    {
      _record.setLongField(Attributes.CREATE_TIMESTAMP.toString(), new Date().getTime());
    }
  }

  public void setCreateTimeStamp(long timestamp)
  {
    _record.setLongField(Attributes.CREATE_TIMESTAMP.toString(), timestamp);
  }

  public Message(ZNRecord record, String id)
  {
    super(new ZNRecord(record, id));
    setMsgId(id);
  }

  public void setMsgSubType(String subType)
  {
    _record.setSimpleField(Attributes.MSG_SUBTYPE.toString(), subType);
  }

  public String getMsgSubType()
  {
    return _record.getSimpleField(Attributes.MSG_SUBTYPE.toString());
  }

  void setMsgType(MessageType type)
  {
    _record.setSimpleField(Attributes.MSG_TYPE.toString(), type.toString());
  }

  public String getMsgType()
  {
    return _record.getSimpleField(Attributes.MSG_TYPE.toString());
  }

  public String getTgtSessionId()
  {
    return _record.getSimpleField(Attributes.TGT_SESSION_ID.toString());
  }

  public void setTgtSessionId(String tgtSessionId)
  {
    _record.setSimpleField(Attributes.TGT_SESSION_ID.toString(), tgtSessionId);
  }

  public String getSrcSessionId()
  {
    return _record.getSimpleField(Attributes.SRC_SESSION_ID.toString());
  }

  public void setSrcSessionId(String srcSessionId)
  {
    _record.setSimpleField(Attributes.SRC_SESSION_ID.toString(), srcSessionId);
  }

  public String getExecutionSessionId()
  {
    return _record.getSimpleField(Attributes.EXE_SESSION_ID.toString());
  }

  public void setExecuteSessionId(String exeSessionId)
  {
    _record.setSimpleField(Attributes.EXE_SESSION_ID.toString(), exeSessionId);
  }

  public String getMsgSrc()
  {
    return _record.getSimpleField(Attributes.SRC_NAME.toString());
  }

  public void setSrcInstanceType(InstanceType type)
  {
    _record.setEnumField(Attributes.SRC_INSTANCE_TYPE.toString(), type);
  }

  public InstanceType getSrcInstanceType()
  {
    return _record.getEnumField(Attributes.SRC_INSTANCE_TYPE.toString(),
        InstanceType.class, InstanceType.PARTICIPANT);
  }

  public void setSrcName(String msgSrc)
  {
    _record.setSimpleField(Attributes.SRC_NAME.toString(), msgSrc);
  }

  public String getTgtName()
  {
    return _record.getSimpleField(Attributes.TGT_NAME.toString());
  }

  public void setMsgState(MessageState msgState)
  { // HACK: The "tolowerCase()" call is to make the change backward compatible
    _record.setSimpleField(Attributes.MSG_STATE.toString(), msgState.toString()
                                                                    .toLowerCase());
  }

  public MessageState getMsgState()
  {
    // HACK: The "toUpperCase()" call is to make the change backward compatible
    return MessageState.valueOf(_record.getSimpleField(Attributes.MSG_STATE.toString())
                                       .toUpperCase());
  }

  public void setPartitionName(String partitionName)
  {
    _record.setSimpleField(Attributes.PARTITION_NAME.toString(), partitionName);
  }

  public String getMsgId()
  {
    return _record.getSimpleField(Attributes.MSG_ID.toString());
  }

  public void setMsgId(String msgId)
  {
    _record.setSimpleField(Attributes.MSG_ID.toString(), msgId);
  }

  public void setFromState(String state)
  {
    _record.setSimpleField(Attributes.FROM_STATE.toString(), state);
  }

  public String getFromState()
  {
    return _record.getSimpleField(Attributes.FROM_STATE.toString());
  }

  public void setToState(String state)
  {
    _record.setSimpleField(Attributes.TO_STATE.toString(), state);
  }

  public String getToState()
  {
    return _record.getSimpleField(Attributes.TO_STATE.toString());
  }

  public void setTgtName(String msgTgt)
  {
    _record.setSimpleField(Attributes.TGT_NAME.toString(), msgTgt);
  }

  public Boolean getDebug()
  {
    return false;
  }

  public Integer getGeneration()
  {
    return 1;
  }

  public void setResourceName(String resourceName)
  {
    _record.setSimpleField(Attributes.RESOURCE_NAME.toString(), resourceName);
  }

  public String getResourceName()
  {
    return _record.getSimpleField(Attributes.RESOURCE_NAME.toString());
  }

  public String getPartitionName()
  {
    return _record.getSimpleField(Attributes.PARTITION_NAME.toString());
  }

  public String getStateModelDef()
  {
    return _record.getSimpleField(Attributes.STATE_MODEL_DEF.toString());
  }

  public void setStateModelDef(String stateModelDefName)
  {
    _record.setSimpleField(Attributes.STATE_MODEL_DEF.toString(), stateModelDefName);
  }

  public void setReadTimeStamp(long time)
  {
    _record.setLongField(Attributes.READ_TIMESTAMP.toString(), time);
  }

  public void setExecuteStartTimeStamp(long time)
  {
    _record.setLongField(Attributes.EXECUTE_START_TIMESTAMP.toString(), time);
  }

  public long getReadTimeStamp()
  {
    return _record.getLongField(Attributes.READ_TIMESTAMP.toString(), 0L);
  }

  public long getExecuteStartTimeStamp()
  {
    return _record.getLongField(Attributes.EXECUTE_START_TIMESTAMP.toString(), 0L);
  }

  public long getCreateTimeStamp()
  {
    return _record.getLongField(Attributes.CREATE_TIMESTAMP.toString(), 0L);
  }

  public void setCorrelationId(String correlationId)
  {
    _record.setSimpleField(Attributes.CORRELATION_ID.toString(), correlationId);
  }

  public String getCorrelationId()
  {
    return _record.getSimpleField(Attributes.CORRELATION_ID.toString());
  }

  public int getExecutionTimeout()
  {
    return _record.getIntField(Attributes.TIMEOUT.toString(), -1);
  }

  public void setExecutionTimeout(int timeout)
  {
    _record.setIntField(Attributes.TIMEOUT.toString(), timeout);
  }

  public void setRetryCount(int retryCount)
  {
    _record.setIntField(Attributes.RETRY_COUNT.toString(), retryCount);
  }

  public int getRetryCount()
  {
    return _record.getIntField(Attributes.RETRY_COUNT.toString(), 0);
  }

  public Map<String, String> getResultMap()
  {
    return _record.getMapField(Attributes.MESSAGE_RESULT.toString());
  }

  public void setResultMap(Map<String, String> resultMap)
  {
    _record.setMapField(Attributes.MESSAGE_RESULT.toString(), resultMap);
  }

  public String getStateModelFactoryName()
  {
    return _record.getSimpleField(Attributes.STATE_MODEL_FACTORY_NAME.toString());
  }

  public void setStateModelFactoryName(String factoryName)
  {
    _record.setSimpleField(Attributes.STATE_MODEL_FACTORY_NAME.toString(), factoryName);
  }

  // TODO: remove this. impl in HelixProperty
  @Override
  public int getBucketSize()
  {
    return _record.getIntField(Attributes.BUCKET_SIZE.toString(), 0);
  }

  @Override
  public void setBucketSize(int bucketSize)
  {
    if (bucketSize > 0)
    {
      _record.setIntField(Attributes.BUCKET_SIZE.toString(), bucketSize);
    }
  }

  public void setAttribute(Attributes attr, String val)
  {
    _record.setSimpleField(attr.toString(), val);
  }
  
  public String getAttribute(Attributes attr)
  {
    return _record.getSimpleField(attr.toString());
  }
  
  public static Message createReplyMessage(Message srcMessage,
                                           String instanceName,
                                           Map<String, String> taskResultMap)
  {
    if (srcMessage.getCorrelationId() == null)
    {
      throw new HelixException("Message " + srcMessage.getMsgId()
          + " does not contain correlation id");
    }
    Message replyMessage =
        new Message(MessageType.TASK_REPLY, UUID.randomUUID().toString());
    replyMessage.setCorrelationId(srcMessage.getCorrelationId());
    replyMessage.setResultMap(taskResultMap);
    replyMessage.setTgtSessionId("*");
    replyMessage.setMsgState(MessageState.NEW);
    replyMessage.setSrcName(instanceName);
    if (srcMessage.getSrcInstanceType() == InstanceType.CONTROLLER)
    {
      replyMessage.setTgtName("Controller");
    }
    else
    {
      replyMessage.setTgtName(srcMessage.getMsgSrc());
    }
    return replyMessage;
  }

  public void addPartitionName(String partitionName)
  {
    if (_record.getListField(Attributes.PARTITION_NAME.toString()) == null)
    {
      _record.setListField(Attributes.PARTITION_NAME.toString(), new ArrayList<String>());
    }
    
    List<String> partitionNames = _record.getListField(Attributes.PARTITION_NAME.toString());
    if (!partitionNames.contains(partitionName))
    {
      partitionNames.add(partitionName);
    }
  }

  public List<String> getPartitionNames()
  {
    List<String> partitionNames =
        _record.getListField(Attributes.PARTITION_NAME.toString());
    if (partitionNames == null)
    {
      return Collections.emptyList();
    }

    return partitionNames;
  }

//  public AtomicInteger getGroupMsgCountDown()
//  {
//    return _groupMsgCountDown;
//  }
//  
//  public void setGroupMsgCountDown(AtomicInteger countDown)
//  {
//    _groupMsgCountDown = countDown;
//  }
  
  public boolean isControlerMsg()
  {
    return getTgtName().equalsIgnoreCase("controller");
  }
  
  public PropertyKey getKey(Builder keyBuilder, String instanceName)
  {
    if (isControlerMsg())
    {
      return keyBuilder.controllerMessage(getId());
    }
    else
    {
      return keyBuilder.message(instanceName, getId());
    }
  }
  
  private boolean isNullOrEmpty(String data)
  {
    return data == null || data.length() == 0 || data.trim().length() == 0;
  }

  @Override
  public boolean isValid()
  {
    // TODO: refactor message to state transition message and task-message and
    // implement this function separately

    if (getMsgType().equals(MessageType.STATE_TRANSITION.toString()))
    {
      boolean isNotValid =
          isNullOrEmpty(getTgtName()) || isNullOrEmpty(getPartitionName())
              || isNullOrEmpty(getResourceName()) || isNullOrEmpty(getStateModelDef())
              || isNullOrEmpty(getToState()) || isNullOrEmpty(getStateModelFactoryName())
              || isNullOrEmpty(getFromState());

      return !isNotValid;
    }

    return true;
  }
}
