//package com.cookiek.commenthat.domain;
//
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import static jakarta.persistence.FetchType.LAZY;
//
//@Entity
//@Getter @Setter
//@Table(name = "reference")
//public class Reference {
//
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "reference_id")
//    private Long id;
//
//    private String topic;
//
//    private String url1;
//    private String url2;
//    private String url3;
//    private String url4;
//    private String url5;
//
//    @ManyToOne(fetch = LAZY)
//    @JoinColumn(name = "contents_id")
//    private Contents contents;
//
//}
