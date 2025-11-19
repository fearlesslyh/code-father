package com.lyh.codefather.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lyh.codefather.model.entity.App;
import com.lyh.codefather.generator.mapper.AppMapper;
import com.lyh.codefather.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @since ${DATE} $TIME
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
