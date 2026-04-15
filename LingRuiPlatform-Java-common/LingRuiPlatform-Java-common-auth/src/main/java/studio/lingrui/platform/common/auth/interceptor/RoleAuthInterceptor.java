package studio.lingrui.platform.common.auth.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import studio.lingrui.platform.common.auth.annotation.HasRole;
import studio.lingrui.platform.common.auth.bo.JwtClaimsBO;
import studio.lingrui.platform.common.auth.bo.UserBO;
import studio.lingrui.platform.common.auth.enums.RoleEnums;
import studio.lingrui.platform.common.auth.utils.JwtUtil;
import studio.lingrui.platform.common.auth.utils.UserContext;
import studio.lingrui.platform.common.core.exception.ForbiddenException;
import studio.lingrui.platform.common.core.exception.LoginException;
import studio.lingrui.platform.common.dao.role_of_user.mapper.RoleOfUserMapper;
import studio.lingrui.platform.common.redis.constant.RedisEntityPrefix;
import studio.lingrui.platform.common.redis.constant.RedisServicePrefix;
import studio.lingrui.platform.common.redis.entity.UserSession;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 2
 * @date 2026/3/15
 * @description: 角色认证拦截器
 */

@Component
public class RoleAuthInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    private final JwtUtil jwtUtil;

    private final RoleOfUserMapper roleOfUserMapper;

    public RoleAuthInterceptor(StringRedisTemplate stringRedisTemplate, JwtUtil jwtUtil, RoleOfUserMapper roleOfUserMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtUtil = jwtUtil;
        this.roleOfUserMapper = roleOfUserMapper;
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,@Nonnull HttpServletResponse response,@Nonnull Object handler) throws ForbiddenException {
        if(!(handler instanceof HandlerMethod method)) {
            // 如果不是HandlerMethod，直接放行
            return true;
        }

        //从方法中获取角色信息，如果没有角色信息，就从类中获取
        HasRole hasRole = getRole(method);
        if (hasRole == null) {
            // 如果没有角色信息，直接放行
            return true;
        }

        //获取请求头中的token中的用户
        String token = request.getHeader("authorization");

        if (StrUtil.isBlank(token)) {
            throw new LoginException();
        }

        //解析token
        JwtClaimsBO jwtClaimsBO = jwtUtil.parseToken(token);
        Long uid = jwtClaimsBO.getUid();



        //防止过期token复用和篡改token
        String userKey = RedisServicePrefix.USER_SERVICE + RedisEntityPrefix.USER_SESSION + uid;
        UserSession userSession = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(userKey), UserSession.class);

        if (userSession == null || !Objects.equals(userSession.getToken(),token)){
            throw new LoginException();
        }

        //用户角色认证
        String userRoleKey = RedisServicePrefix.USER_SERVICE + RedisEntityPrefix.USER_ROLE + uid;
        Set<String> roleStrIds = stringRedisTemplate.opsForSet().members(userRoleKey);

        //从redis中查询缓存
        Set<Long> roleIds = roleStrIds == null ? new HashSet<>() :
                roleStrIds.stream()
                        .map(Long::valueOf)
                        .collect(Collectors.toSet());

        if (roleIds.isEmpty()){
            //从数据库中查询有效的角色信息
            roleIds = roleOfUserMapper.selectRoleIdsByUid(uid);
            //缓存角色信息
            stringRedisTemplate.opsForSet().add(userRoleKey,roleIds.stream().map(String::valueOf).toArray(String[]::new));
        }

        if (roleIds.isEmpty()){
            throw new ForbiddenException();
        }

        //刷新过期时间
        stringRedisTemplate.expire(userRoleKey,7, TimeUnit.DAYS);

        //放入用户上下文
        UserBO userBO = new UserBO()
                .setUid(uid);
        UserContext.saveUser(userBO);

        //遍历hasRole直到出现有效角色
        for (RoleEnums roleEnums : hasRole.value()) {
            if (roleIds.contains(roleEnums.getId())){
                return true;
            }
        }

        //无符合角色,抛异常
        throw new ForbiddenException();
    }

    private HasRole getRole(HandlerMethod method){
        HasRole hasRole = method.getMethodAnnotation(HasRole.class);
        if (hasRole == null){
            hasRole = method.getBeanType().getAnnotation(HasRole.class);
        }
        return hasRole;
    }


    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request,
                                @Nonnull HttpServletResponse response,
                                @Nonnull Object handler,
                                Exception ex) {
        //清空用户上下文,防止线程复用出现问题
        UserContext.removeUser();
    }
}