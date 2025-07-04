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
 */
@Slf4j
public class BaseService<E>{

    @Autowired
    protected BaseMapper<E> baseMapper;//使用MyBatis的Basemapper自动继承方法

    Class<E> eClass = (Class<E>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    private final String table = humpToLine(eClass.getSimpleName());

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

    public void insert(Map<String,Object> body){
        E entity = JSON.parseObject(JSON.toJSONString(body),eClass);
        baseMapper.insert(entity);
        log.info("[{}] - 插入操作：{}",entity);
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

    public Integer selectSqlToInteger(String sql){
        Integer value = baseMapper.selectBaseCount(sql);
        return value;
    }

    public Map<String,Object> selectBarGroup(Map<String,String> query,Map<String,String> config){
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> resultList = baseMapper.selectBaseList(barGroup(query, config));
        List list = new ArrayList();
        for (Map<String,Object> resultMap:resultList) {
            List subList = new ArrayList();
            for(String key:resultMap.keySet()){//keySet获取map集合key的集合  然后在遍历key即可
                subList.add(resultMap.get(key));
            }
            list.add(subList);
        }
        map.put("list",list);
        return map;
    }

//    public void barGroup(Map<String,String> query,Map<String,String> config,QueryWrapper wrapper){
//        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
//            wrapper.select(config.get(FindConfig.GROUP_BY));
//            if (config.get(FindConfig.FIELD) != null && !"".equals(config.get(FindConfig.FIELD))){
//                String[] fieldList = config.get(FindConfig.FIELD).split(",");
//                for (int i=0;i<fieldList.length;i++)
//                    wrapper.select("SUM("+fieldList[i]+")");
//            }
//            toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),wrapper);
//            wrapper.groupBy(config.get(FindConfig.GROUP_BY));
//        }else {
//            wrapper.select("SUM("+config.get(FindConfig.GROUP_BY)+")");
//            toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),wrapper);
//        }
//        log.info("[{}] - 查询操作，sql: {}",wrapper.getSqlSelect());
//    }

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

//    public void selectGroupCount(Map<String,String> query,Map<String,String> config,QueryWrapper wrapper){
//        wrapper.select("count(*) AS count_value",config.get(FindConfig.GROUP_BY));
//        toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),wrapper);
//        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
//            wrapper.groupBy(config.get(FindConfig.GROUP_BY));
//        }
//        log.info("[{}] - 查询操作，sql: {}",wrapper.getSqlSelect());
//    }

    public String selectGroupCount(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("select COUNT(*) AS count, ");
        sql.append(config.get(FindConfig.GROUP_BY)).append(" ");
        sql.append("from ").append("`").append(table).append("` ");
        sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("group by ").append(config.get(FindConfig.GROUP_BY)).append(" ");
        }
        log.info("[{}] - 查询操作，sql: {}",table,sql);
        return sql.toString();
    }

//    public void select(Map<String,String> query,Map<String,String> config,QueryWrapper wrapper){
//        wrapper.select(config.get(FindConfig.FIELD) == null || "".equals(config.get(FindConfig.FIELD)) ? "*" : config.get(FindConfig.FIELD));
//        toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),wrapper);
//        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
//            wrapper.groupBy(config.get(FindConfig.GROUP_BY));
//        }
//        if (config.get(FindConfig.ORDER_BY) != null && !"".equals(config.get(FindConfig.ORDER_BY))){
//            if (config.get(FindConfig.ORDER_BY).toUpperCase().contains("DESC")){
//                wrapper.orderByDesc(config.get(FindConfig.ORDER_BY).toUpperCase().replaceAll(" DESC",""));
//            }else {
//                wrapper.orderByAsc(config.get(FindConfig.ORDER_BY).toUpperCase().replaceAll(" ASC",""));
//            }
//        }
//        if (config.get(FindConfig.PAGE) != null && !"".equals(config.get(FindConfig.PAGE))){
//            int page = config.get(FindConfig.PAGE) != null && !"".equals(config.get(FindConfig.PAGE)) ? Integer.parseInt(config.get(FindConfig.PAGE)) : 1;
//            int limit = config.get(FindConfig.SIZE) != null && !"".equals(config.get(FindConfig.SIZE)) ? Integer.parseInt(config.get(FindConfig.SIZE)) : 10;
//            wrapper.last("limit "+(page-1)*limit+" , "+limit);
//        }
//        log.info("[{}] - 查询操作，sql: {}",wrapper.getSqlSelect());
//    }

    public String select(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("select ");

        // 如果是查询 article 表，动态计算点赞数
        if ("article".equals(table)) {
            sql.append("a.*, COALESCE(p.praise_count, 0) as praise_len ");
            sql.append("from `").append(table).append("` a ");
            sql.append("LEFT JOIN (");
            sql.append("  SELECT source_id, COUNT(*) as praise_count ");
            sql.append("  FROM praise ");
            sql.append("  WHERE source_table = 'article' AND status = 1 ");
            sql.append("  GROUP BY source_id");
            sql.append(") p ON a.article_id = p.source_id ");

            // 为 article 表单独处理 WHERE 条件
            String whereSql = toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)), config.get(FindConfig.SQLHWERE));
            if (!whereSql.isEmpty()) {
                sql.append(whereSql);
            }
        } else {
            sql.append(config.get(FindConfig.FIELD) == null || "".equals(config.get(FindConfig.FIELD)) ? "*" : config.get(FindConfig.FIELD)).append(" ");
            sql.append("from ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        }

        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("group by ").append(config.get(FindConfig.GROUP_BY)).append(" ");
        }
        // 新增：content_reporting 默认按 create_time 降序排序
        String orderBy = config.get(FindConfig.ORDER_BY);
        if ((orderBy == null || "".equals(orderBy)) && "content_reporting".equals(table)) {
            orderBy = "create_time desc";
        }
        if (orderBy != null && !"".equals(orderBy)) {
            sql.append("order by ").append(orderBy).append(" ");
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

//    public void count(Map<String,String> query,Map<String,String> config, QueryWrapper wrapper){
////        log.info("拼接统计函数前");
//        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
//            wrapper.select(config.get(FindConfig.GROUP_BY));
//            wrapper.select("COUNT("+config.get(FindConfig.GROUP_BY)+")");
//            toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),wrapper);
//        }else {
//            wrapper.select("COUNT(*)");
//            toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),wrapper);
//        }
//        log.info("[{}] - 统计操作，sql: {}",wrapper.getSqlSelect());
//    }

    public String count(Map<String,String> query,Map<String,String> config){
        StringBuffer sql = new StringBuffer("SELECT ");
//        log.info("拼接统计函数前");
        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
            sql.append("COUNT(").append(config.get(FindConfig.GROUP_BY)).append(") FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
//            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
        }else {
            sql.append("COUNT(*) FROM ").append("`").append(table).append("`");
            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE)),config.get(FindConfig.SQLHWERE)));
        }
        log.info("[{}] - 统计操作，sql: {}",table,sql);
        return sql.toString();
    }

//    public Query sum(Map<String,String> query,Map<String,String> config){
//        StringBuffer sql = new StringBuffer(" SELECT ");
//        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
//            sql.append(config.get(FindConfig.GROUP_BY)).append(" ,SUM(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
//            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
//            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
//        }else {
//            sql.append(" SUM(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
//            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
//        }
//        log.info("[{}] - 查询操作，sql: {}",table,sql);
//        return runCountSql(sql.toString());
//    }
//
//    public Query avg(Map<String,String> query,Map<String,String> config){
//        StringBuffer sql = new StringBuffer(" SELECT ");
//        if (config.get(FindConfig.GROUP_BY) != null && !"".equals(config.get(FindConfig.GROUP_BY))){
//            sql.append(config.get(FindConfig.GROUP_BY)).append(" ,AVG(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
//            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
//            sql.append(" ").append("GROUP BY ").append(config.get(FindConfig.GROUP_BY));
//        }else {
//            sql.append(" AVG(").append(config.get(FindConfig.FIELD)).append(") FROM ").append("`").append(table).append("`");
//            sql.append(toWhereSql(query, "0".equals(config.get(FindConfig.LIKE))));
//        }
//        log.info("[{}] - 查询操作，sql: {}",table,sql);
//        return runCountSql(sql.toString());
//    }

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
                    // 跳过空字符串和null的参数
                    if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                        continue;
                    }
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
                        sql.append("`"+humpToLine(entry.getKey())+"`").append(" LIKE '%").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("%' and ");
                    } else {
                        sql.append("`"+humpToLine(entry.getKey())+"`").append(" = '").append(URLDecoder.decode(entry.getValue(), "UTF-8")).append("' and ");
                    }
                }
                if (sqlwhere!=null && !sqlwhere.trim().equals("")) {
                    sql.append(sqlwhere).append(" and ");
                }
                if (sql.length() > 7) { // " WHERE "长度为7
                    sql.delete(sql.length() - 4, sql.length());
                    sql.append(" ");
                    return sql.toString();
                } else if (sqlwhere!=null && !sqlwhere.trim().equals("")) {
                    return " WHERE " + sqlwhere;
                } else {
                    return "";
                }
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

    public List<Map<String, Object>> selectGroupCountList(Map<String, String> query, Map<String, String> config) {
        String sql = selectGroupCount(query, config);
        return baseMapper.selectBaseList(sql);
    }

}
