package com.jobatda.back.domain.board.entity;

/**
 * 게시판 타입을 정의하는 열거형(Enum)
 * 
 * 게시글의 분류를 위해 사용되며, 각 게시글은 반드시 하나의 타입을 가져야 함
 * 데이터베이스에는 문자열로 저장됨 (@Enumerated(EnumType.STRING))
 */
public enum BoardType {
    /**
     * 공지사항 게시판
     * 관리자가 사용자에게 중요한 정보를 전달하는 용도
     */
    NOTICE,  
    
    /**
     * 기업게시판
     * 기업 관련 정보, 채용공고, 기업 소식 등을 공유하는 게시판
     */
    COMPANY,
    
    /**
     * 자유게시판
     * 사용자들이 자유롭게 의견을 나누는 일반적인 게시판
     */
    FREE,    
    
    /**
     * Q&A 게시판 
     * 질문과 답변을 위한 전문 게시판
     */
    QNA      
}
