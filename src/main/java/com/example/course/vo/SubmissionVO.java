package com.example.course.vo;

import com.example.course.entity.Submission;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionVO {
    private Long id;
    private Long assignmentId;
    private String studentId;
    private String content;
    private String attachmentPath;
    private Double score;
    private String comment;
    // 学生对评语的回复
    private String reply;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联信息
    private String assignmentTitle;
    private String courseName;
    private String studentName;

    public static SubmissionVO fromEntity(Submission submission) {
        SubmissionVO vo = new SubmissionVO();
        vo.setId(submission.getId());
        vo.setAssignmentId(submission.getAssignmentId());
        vo.setStudentId(submission.getStudentId());
        vo.setContent(submission.getContent());
        vo.setAttachmentPath(submission.getAttachmentPath());
        vo.setScore(submission.getScore());
        vo.setComment(submission.getComment());
         // 把学生回复也带出去，方便教师端查看
        vo.setReply(submission.getReply());
        vo.setStatus(submission.getStatus());
        vo.setCreateTime(submission.getCreateTime());
        vo.setUpdateTime(submission.getUpdateTime());
        return vo;
    }
}