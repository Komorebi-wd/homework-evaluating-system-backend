package com.example.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Suggestion;
import com.example.mapper.SuggestionMapper;
import com.example.service.SuggestionService;
import org.springframework.stereotype.Service;

@Service
public class SuggestionServiceImpl extends ServiceImpl<SuggestionMapper, Suggestion> implements SuggestionService {
}
