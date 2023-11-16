package com.example.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
     String key;//保存在yml中

    @Value("${spring.security.jwt.expire}")
    int expire;//令牌有效时间，yml中

    @Resource
    StringRedisTemplate template;

    //创建令牌，这里我全部用了String
    public String createJwt(UserDetails userDetails, String id, String username){
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expireTime = this.expireTime();
        return JWT.create()//设置令牌都包含哪些内容
                .withJWTId(UUID.randomUUID().toString())//jwtID, 区分用户id
                .withClaim("id", id)
                .withClaim("username", username)
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                //这里authorities不是很明白，主要在转换格式
                .withExpiresAt(expireTime)//设置有效时间
                .withIssuedAt(new Date())//设置签发时间
                .sign(algorithm);//算法加密
    }

    //将Header中的JWT解析后返回
    public DecodedJWT resolveJWT(String jwtToken){
        String convertToken = this.convertToken(jwtToken);
        if(convertToken == null) return null;//转换后不能为null
        //创建解析器
        Algorithm algorithm = Algorithm.HMAC256(this.key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        //进行解析，同时验证时间
        try {
            DecodedJWT decodedJWT = jwtVerifier.verify(convertToken);
            if(this.isInvalidJwt(decodedJWT.getId())) return null;//如果令牌已失效，返回null
            Date expiresAt = decodedJWT.getExpiresAt();
            return new Date().after(expiresAt) ? null : decodedJWT;//验证时间
        }catch (JWTVerificationException e){//解析错误
            return null;
        }
    }

    //让令牌失效
    //true表成功，false表失败（jwt已在黑名单、jwt错误、验证异常等等）
    public boolean invalidateJwt(String jwtToken){
        String convertToken = this.convertToken(jwtToken);
        if(convertToken == null) return false;
        //创建解析器
        Algorithm algorithm = Algorithm.HMAC256(this.key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT decodedJWT = jwtVerifier.verify(convertToken);
            String jwtId = decodedJWT.getId();//JWTID, 区分用户id
            return setJwtBlack(jwtId, decodedJWT.getExpiresAt());//传入令牌id、失效时间
        }catch (JWTVerificationException e){//解析错误
            return false;
        }
    }

    //将jwt加入黑名单
    //如果还未过期，则更新时间后加入黑名单
    //如果已过期，设为0后加入黑名单
    //true表示成功更新，false表示不需更新（已经加入黑名单的）
    private boolean setJwtBlack(String jwtId, Date time){//time指令牌失效时间
        if(this.isInvalidJwt(jwtId)) return false;//已经在黑名单，不需再set
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);//若expire更新后<=0，则设为0
        //加入黑名单（value是啥不知道）
        template.opsForValue().set(Const.JWT_BLACK_LIST + jwtId, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    //检查redis中黑名单是否有该jwt（true表示invalid）
    //jwt黑名单前缀最为const统一管理
    private boolean isInvalidJwt(String jwtId){
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + jwtId));
        //这里不用Boolean包装一下会黄，似乎以防空指针？
    }

    //设置令牌有效时间
    public Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expire);
        return calendar.getTime();
    }

    //对Header中的Token初步验证、转换
    //必须以Bearer开头，将开头切掉后返回
    private String convertToken(String jwtToken){
        if(jwtToken == null || !jwtToken.startsWith("Bearer ")){
            return null;
        }
        return jwtToken.substring(7);
    }

    //将解析后的jwt中userdetail相关信息取出给前端
    public UserDetails decodedJwtToUser(DecodedJWT decodedJWT){
        Map<String, Claim> claims = decodedJWT.getClaims();
        return User
                .withUsername(claims.get("username").asString())
                .password("xxxxxx")//这里我不太明白，claim当中并没有password，如何获得呢
                .authorities(claims.get("authorities").asArray(String.class))//这个authorities多次出现，后面查一下
                .build();
    }
    //获得jwt中userdetail的id
    public String decodedJwtToId(DecodedJWT decodedJWT){
        Map<String, Claim> claims = decodedJWT.getClaims();
        return claims.get("id").asString();
    }
}
