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

package org.opengoofy.congomall.bff.biz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.congomall.bff.biz.assembler.ProductCartAssembler;
import org.opengoofy.congomall.bff.biz.common.SelectFlagEnum;
import org.opengoofy.congomall.bff.biz.dto.req.adapter.ProductCartAddAdapterReqDTO;
import org.opengoofy.congomall.bff.biz.dto.resp.adapter.ProductCartAdapterRespDTO;
import org.opengoofy.congomall.bff.biz.service.ProductCartService;
import org.opengoofy.congomall.bff.remote.ProductCartRemoteService;
import org.opengoofy.congomall.bff.remote.ProductRemoteService;
import org.opengoofy.congomall.bff.remote.req.CartItemAddReqDTO;
import org.opengoofy.congomall.bff.remote.resp.CartItemRespDTO;
import org.opengoofy.congomall.bff.remote.resp.ProductRespDTO;
import org.opengoofy.congomall.bff.remote.resp.ProductSkuRespDTO;
import org.opengoofy.congomall.bff.remote.resp.ProductSpuRespDTO;
import org.opengoofy.congomall.springboot.starter.common.toolkit.BeanUtil;
import org.opengoofy.congomall.springboot.starter.convention.page.PageResponse;
import org.opengoofy.congomall.springboot.starter.convention.result.Result;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车接口实现层
 *
 * @author chen.ma
 * @github <a href="https://github.com/opengoofy" />
 * @公众号 马丁玩编程，关注回复：资料，领取后端技术专家成长手册
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCartServiceImpl implements ProductCartService {
    
    private final ProductCartRemoteService productCartRemoteService;
    private final ProductCartAssembler productCartAssembler;
    private final ProductRemoteService productRemoteService;
    
    private static final long PRODUCT_CART_CURRENT = 1L;
    private static final long PRODUCT_CART_SIZE = 500L;
    
    @Override
    public List<ProductCartAdapterRespDTO> listAllProductCart(String userId) {
        Result<PageResponse<CartItemRespDTO>> remoteProductResult = null;
        try {
            remoteProductResult = productCartRemoteService.pageQueryCartItem(userId, PRODUCT_CART_CURRENT, PRODUCT_CART_SIZE);
        } catch (Throwable ex) {
            log.error("调用购物车服务查询用户购物车商品失败", ex);
        }
        List<ProductCartAdapterRespDTO> result = new ArrayList<>();
        if (remoteProductResult != null && remoteProductResult.isSuccess()) {
            result = productCartAssembler.covert(remoteProductResult.getData().getRecords());
        }
        return result;
    }
    
    @Override
    public Integer addProductCard(ProductCartAddAdapterReqDTO requestParam) {
        String productId = requestParam.getProductId();
        Result<ProductRespDTO> remoteProductResult = null;
        try {
            remoteProductResult = productRemoteService.getProductBySpuId(productId);
        } catch (Throwable ex) {
            log.error("调用商品服务查询商品详细信息失败", ex);
        }
        Result<Void> addCartResult = null;
        if (remoteProductResult != null && remoteProductResult.isSuccess()) {
            ProductRespDTO productResultData = remoteProductResult.getData();
            ProductSpuRespDTO productSpu = productResultData.getProductSpu();
            ProductSkuRespDTO productSku = productResultData.getProductSkus().get(0);
            CartItemAddReqDTO cartRequestParam = BeanUtil.convert(productSpu, CartItemAddReqDTO.class);
            cartRequestParam.setProductId(String.valueOf(productSpu.getId()));
            cartRequestParam.setProductPic(productSpu.getPic());
            cartRequestParam.setProductName(productSpu.getName());
            cartRequestParam.setProductBrand(String.valueOf(productSpu.getBrandId()));
            cartRequestParam.setProductPrice(productSku.getPrice());
            cartRequestParam.setSelectFlag(SelectFlagEnum.SELECTED.getCode());
            cartRequestParam.setProductSkuId(String.valueOf(productSku.getId()));
            cartRequestParam.setCustomerUserId(requestParam.getUserId());
            cartRequestParam.setProductQuantity(requestParam.getProductNum());
            try {
                addCartResult = productCartRemoteService.addCartItem(cartRequestParam);
            } catch (Throwable ex) {
                log.error("调用购物车服务新增购物车商品失败", ex);
            }
        }
        return (addCartResult == null || !addCartResult.isSuccess()) ? 0 : 1;
    }
}