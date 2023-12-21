package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SimilarHomework {
    String sid1;//被查学生sid
    String sid2;//相似作业sid，选的是最相似的人
    Double value;//相似度0~100
}
