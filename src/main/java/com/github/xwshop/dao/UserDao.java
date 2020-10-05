package com.github.xwshop.dao;

import com.github.xwshop.generate.User;
import com.github.xwshop.generate.UserExample;
import com.github.xwshop.generate.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDao {
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public UserDao(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void insertUser(User user) {
        try(SqlSession sqlSession  =  sqlSessionFactory.openSession(true)){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            mapper.insert(user);
        }
    }

    public User getUserByTel(String tel) {
        try(SqlSession sqlSession  =  sqlSessionFactory.openSession(true)){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);

            UserExample example = new UserExample();
            example.createCriteria().andTelEqualTo(tel);
            return mapper.selectByExample(example).get(0);
        }
    }
}
