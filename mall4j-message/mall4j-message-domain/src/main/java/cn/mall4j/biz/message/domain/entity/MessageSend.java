/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.mall4j.biz.message.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.List;

/**
 * 消息发送 Entity
 */
@Data
@Builder
@Setter(AccessLevel.PRIVATE)
public class MessageSend {
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 发送者
     */
    private String sender;
    
    /**
     * 接收者
     */
    private String receiver;
    
    /**
     * 抄送
     */
    private String cc;
    
    /**
     * 消息参数
     */
    private List<String> paramList;
    
    /**
     * 模板ID
     */
    private String templateId;
    
    /**
     * 消息发送ID
     */
    private String messageSendId;
    
    /**
     * 发送结果
     */
    private Boolean sendResult;
    
    public void setSendResult(boolean sendResult) {
        this.sendResult = sendResult;
    }
}
