package com.project.demo.service.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.demo.constant.FindConfig;
import com.project.demo.dao.base.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.*;

/**
 * BaseService 几乎把"增删改查 + 分页 + 分组 + 原生 SQL + HTTP 参数解析 + JSON/格式转换"都封装好了，子类只需关注业务即可。
 * 既能用 MyBatis-Plus 的强类型 QueryWrapper，也能拼纯 SQL；既能返回实体，也能返回 Map 或二维数组。
 */
@Slf4j
public class BaseService<E>{

    @Autowired
    private BaseMapper<E> baseMapper;

    Class<E> eClass = (Class<E>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    //在子类如 ArticleService extends BaseService<Article> 时，E 就是 Article，eClass 会被反射为 Article.class
    private final String table = humpToLine(eClass.getSimpleName());//把实体类名（如 Article）由驼峰转为下划线（article），方便在手写 SQL 时直接用表名

    public List selectBaseList(String select) {
        List<Map<String,Object>> mapList = baseMapper.selectBaseList(select);
        List<E> list = new ArrayList<>();
        for (Map<String,Object> map:mapList) {
            list.add(JSON.parseObject(JSON.toJSONString(map),eClass));
        }
        return list;
    }

    public List<Map<String,Object>> selectMapBaseList(String select) {
        return baseMapper.selectBaseList(select);
    }

    public int selectBaseCount(String sql) {
        return baseMapper.selectBaseCount(sql);
    }

    public int deleteBaseSql(String sql) {
        return baseMapper.deleteBaseSql(sql);
    }

    public int updateBaseSql(String sql) {
        return baseMapper.updateBaseSql(sql);
    }

    public Integer insert(Map<String,Object> body){
        E entity = JSON.parseObject(JSON.toJSONString(body),eClass);
        baseMapper.insert(entity);
        try {
            for (java.lang.reflect.Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(com.baomidou.mybatisplus.annotation.TableId.class)) {
                    field.setAccessible(true);
                    return (Integer) field.get(entity);
                }
            }
            // Fallback or error handling if no @TableId is found
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public void update(Map<String,String> query,Map<String,String> config,Map<String,Object> body){
        QueryWrapper wrapper = new QueryWrapper<E>();
        toWhereWrapper(query,"0".equals(config.get(FindConfig.LIKE)),wrapper);
        E entity = JSON.parseObject(JSON.toJSONString(body),eClass);
        baseMapper.update(entity,wrapper);
        log.info("[{}] - 更新操作：{}",entity);
    }

    public Map<String,Object> selectToPage(Map<String,String> query,Map<String,String> config){
        Map<String,Object> map = new HashMap<>();
        List list = baseMapper.selectBaseList(select(query, config));
        map.put("list",list);
        map.put("count",baseMapper.selectBaseCount(count(query,config)));
        return map;
    }

    public Map<String,Object> selectToList(Map<String,String> query,Map<String,String> config){
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> resultList = baseMapper.selectBaseList(selectGroupCount(query, config));
        for (Map<String,Object> sub:resultList) {
            sub.put("0",sub.get("count"));
            sub.put("1",sub.get(config.get(FindConfig.GROUP_BY)));
        }
        map.put("list",resultList);
        return map;
    }

    public Integer selectSqlToInteger(String sql) {
        List<Map<String, Object>> result = baseMapper.selectBaseList(sql);
        if (result == null || result.isEmpty() || result.get(0) == null || result.get(0).isEmpty()) {
            return 0;
        }
        Object value = result.get(0).values().iterator().next();
        if (value instanceof Number) {
            return (int) Math.round(((Number) value).doubleValue());
        } else if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public Map<String,Object> selectBarGroup(Map<String,String> query,Map<String,String> config){
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> resultList = baseMapper.selectBaseList(barGroup(query, config));
        if (resultList == null) {
            resultList = new ArrayList<>();
        }
        List list = new ArrayList();
        for (Map<String,Object> resultMap:resultList) {
            if (resultMap != null) {
                List subList = new ArrayList();
                for (String key : resultMap.keySet()) {
                    subList.add(resultMap.get(key));
                }
                list.add(subList);
            }
        }
        map.put("list",list);
        return map;
    }

    public String barGroup(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer(" SELECT ");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append(config.get(FindConfig.GROUP_BY));
            if (config.get(FindConfig.FIELD) != null && !"".equals(config.get(FindConfig.FIELD))){
                String[] fieldList = config.get(FindConfig.FIELD).split(",");
                for (int i=0;i<fieldList.length;i++)
                    sql.append(" ,SUM(").append(fieldList[i]).append(")");
            }
            sql.append(" FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append(" SUM(").append(config.get(FindConfig.GROUP_BY)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return sql.toString();
    }

    public String selectGroupCount(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("select COUNT(*) AS count");
        String groupByField = config.get(FindConfig.GROUP_BY);
        if (groupByField != null && !groupByField.trim().isEmpty()) {
            sql.append(", ").append(groupByField).append(" AS ").append(groupByField);
        }
        sql.append(" from ").append("`").append(table).append("`");
        sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        if (groupByField != null && !groupByField.trim().isEmpty()){
            sql.append(" group by ").append(groupByField);
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return sql.toString();
    }

    public String select(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("select ");
        sql.append(config.get(FindConfig.FIELD) == null || "".equals(config.get(FindConfig.FIELD)) ? "*" : config.get(FindConfig.FIELD)).append(" ");
        sql.append("from ").append("`").append(table).append("`").append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("group by ").append(config.get(FindConfig.GROUP_BY)).append(" ");
        }
        if (config.get(FindConfig.ORDER_BY) != null && !"".equals(config.get(FindConfig.ORDER_BY))){
            sql.append("order by ").append(config.get(FindConfig.ORDER_BY)).append(" ");
        }
        if (config.get(FindConfig.PAGE) != null && !"".equals(config.get(FindConfig.PAGE))){
            int page = config.get(FindConfig.PAGE) != null && !"".equals(config.get(FindConfig.PAGE)) ? Integer.parseInt(config.get(FindConfig.PAGE)) : 1;
            int limit = config.get(FindConfig.SIZE) != null && !"".equals(config.get(FindConfig.SIZE)) ? Integer.parseInt(config.get(FindConfig.SIZE)) : 10;
            sql.append(" limit ").append( (page-1)*limit ).append(" , ").append(limit);
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return sql.toString();
    }

    @Transactional
    public void delete(Map<String,String> query,Map<String,String> config){
        QueryWrapper wrapper = new QueryWrapper<E>();
        toWhereWrapper(query, "0".equals(config.get(FindConfig.GROUP_BY)),wrapper);
        baseMapper.delete(wrapper);
        log.info("[{}] - 删除操作：{}",wrapper.getSqlSelect());
    }

    public String count(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("SELECT ");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("COUNT(").append(config.get(FindConfig.GROUP_BY)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        }else {
            sql.append("COUNT(*) FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        }
        log.info("[{}] - 统计操作，sql: {}",table,sql);
        return sql.toString();
    }

    public String groupCount(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("SELECT ");
        log.info("拼接统计函数前");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("COUNT(").append(config.get(FindConfig.GROUP_BY)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append("COUNT(*) FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        }
        log.info("[{}] - 统计操作，sql: {}",table,sql);
        return sql.toString();
    }

    public String sum(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer(" SELECT ");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("SUM(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append(" SUM(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return sql.toString();
    }

    public String avg(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer(" SELECT ");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("AVG(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append(" AVG(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return sql.toString();
    }

    public void toWhereWrapper(Map<String,String> query, Boolean like, QueryWrapper wrapper) {
        if (query.size() > 0) {
            try {
                for (Map.Entry<String, String> entry : query.entrySet()) {
                    if (entry.getKey().contains(FindConfig.MIN_)) {
                        String min = humpToLine(entry.getKey()).replace("_min", "");
                        wrapper.ge(min,URLDecoder.decode(entry.getValue(), "UTF-8"));
                        continue;
                    }
                    if (entry.getKey().contains(FindConfig.MAX_)) {
                        String max = humpToLine(entry.getKey()).replace("_max", "");
                        wrapper.le(max,URLDecoder.decode(entry.getValue(), "UTF-8"));
                        continue;
                    }
                    if (like == true) {
                        if (entry.getValue()!=null)
                            wrapper.like(humpToLine(entry.getKey()),"%"+URLDecoder.decode(entry.getValue(), "UTF-8")+"%");
                    } else {
                        if (entry.getValue()!=null)
                            wrapper.eq(humpToLine(entry.getKey()),URLDecoder.decode(entry.getValue(), "UTF-8"));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                log.info("拼接sql 失败：{}", e.getMessage());
            }
        }
    }

    public String toWhereSql(Map<String,String> query, Boolean like,String sqlwhere) {
        if (query.size() > 0) {
            try {
                StringBuilder sql = new StringBuilder(" WHERE ");
                for (Map.Entry<String, String> entry : query.entrySet()) {
                    if (entry.getKey().contains(FindConfig.MIN_)) {
                        String min = humpToLine(entry.getKey()).replace("_min", "");
                        sql.append("`"+min+"`").append(" >= '").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("' and ");
                        continue;
                    }
                    if (entry.getKey().contains(FindConfig.MAX_)) {
                        String max = humpToLine(entry.getKey()).replace("_max", "");
                        sql.append("`"+max+"`").append(" <= '").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("' and ");
                        continue;
                    }
                    if (like == true) {
                        sql.append("`"+humpToLine(entry.getKey())+"`").append(" LIKE '%").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("%'").append(" and ");
                    } else {
                        sql.append("`"+humpToLine(entry.getKey())+"`").append(" = '").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("'").append(" and ");
                    }
                }
                if (sqlwhere!=null && !sqlwhere.trim().equals("")) {
                    sql.append(sqlwhere).append(" and ");
                }
                sql.delete(sql.length() - 4, sql.length());
                sql.append(" ");
                return sql.toString();
            } catch (UnsupportedEncodingException e) {
                log.info("拼接sql 失败：{}", e.getMessage());
            }
        }else {
            if (sqlwhere!=null && !sqlwhere.trim().equals("")) {
                StringBuilder sql = new StringBuilder(" WHERE ");
                sql.append(sqlwhere);
                return sql.toString();
            }
        }
        return "";
    }

    public Map<String,Object> readBody(BufferedReader reader){
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try{
            br = reader;
            String str;
            while ((str = br.readLine()) != null){
                sb.append(str);
            }
            br.close();
            String json = sb.toString();
            return JSONObject.parseObject(json, Map.class);
        }catch (IOException e){
            e.printStackTrace();
        }finally{
            if (null != br){
                try{
                    br.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Map<String,String> readQuery(HttpServletRequest request){
        String queryString = request.getQueryString();
        if (queryString != null && !"".equals(queryString)) {
            String[] querys = queryString.split("&");
            Map<String, String> map = new HashMap<>();
            for (String query : querys) {
                String[] q = query.split("=");
                map.put(q[0], q[1]);
            }
            map.remove(FindConfig.PAGE);
            map.remove(FindConfig.SIZE);
            map.remove(FindConfig.LIKE);
            map.remove(FindConfig.ORDER_BY);
            map.remove(FindConfig.FIELD);
            map.remove(FindConfig.GROUP_BY);
            map.remove(FindConfig.MAX_);
            map.remove(FindConfig.MIN_);
            map.remove(FindConfig.SQLHWERE);
            return map;
        }else {
            return new HashMap<>();
        }
    }

    public Map<String,String> readConfig(HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        map.put(FindConfig.PAGE,request.getParameter(FindConfig.PAGE));
        map.put(FindConfig.SIZE,request.getParameter(FindConfig.SIZE));
        map.put(FindConfig.LIKE,request.getParameter(FindConfig.LIKE));
        map.put(FindConfig.ORDER_BY,request.getParameter(FindConfig.ORDER_BY));
        map.put(FindConfig.FIELD,request.getParameter(FindConfig.FIELD));
        map.put(FindConfig.GROUP_BY,request.getParameter(FindConfig.GROUP_BY));
        map.put(FindConfig.MAX_,request.getParameter(FindConfig.MAX_));
        map.put(FindConfig.MIN_,request.getParameter(FindConfig.MIN_));
        map.put(FindConfig.SQLHWERE,request.getParameter(FindConfig.SQLHWERE));
        return map;
    }

    @Transactional
    public void save(E e){
        String s = JSONObject.toJSONString(e);
        Map map = JSONObject.parseObject(s, Map.class);
        insert(map);
    }

    public E findOne(Map<String, String> map){
        try {
            return (E)baseMapper.selectBaseOne(select(map, new HashMap<>()));
        }catch (Exception e){
            return null;
        }
    }


    public String encryption(String plainText) {
        String re_md5 = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return re_md5;
    }


    public static String humpToLine(String str) {
        if (str == null) {
            return null;
        }
        // 将驼峰字符串转换成数组
        char[] charArray = str.toCharArray();
        StringBuilder buffer = new StringBuilder();
        //处理字符串
        for (int i = 0, l = charArray.length; i < l; i++) {
            if (charArray[i] >= 65 && charArray[i] <= 90) {
                buffer.append("_").append(charArray[i] += 32);
            } else {
                buffer.append(charArray[i]);
            }
        }
        String s = buffer.toString();
        if (s.startsWith("_")){
            return s.substring(1);
        }else {
            return s;
        }
    }


    public JSONObject covertObject(JSONObject object) {
        if (object == null) {
            return null;
        }
        JSONObject newObject = new JSONObject();
        Set<String> set = object.keySet();
        for (String key : set) {
            Object value = object.get(key);
            if (value instanceof JSONArray) {
                //数组
                value = covertArray(object.getJSONArray(key));
            } else if (value instanceof JSONObject) {
                //对象
                value = covertObject(object.getJSONObject(key));
            }
            //这个方法自己写的改成下划线
            key = humpToLine(key);
            newObject.put(key, value);
        }
        return newObject;
    }

    public JSONArray covertArray(JSONArray array) {
        if (array == null) {
            return null;
        }
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < array.size(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                //数组
                value = covertArray(array.getJSONArray(i));
            } else if (value instanceof JSONObject) {
                //对象
                value = covertObject(array.getJSONObject(i));
            }
            newArray.add(value);
        }
        return newArray;
    }


}
