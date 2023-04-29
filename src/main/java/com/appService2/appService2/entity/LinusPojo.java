package com.appService2.appService2.entity;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinusPojo {

    private String user;
    private String pid;
    private String cpu;
    private String mem;
    private String vsz;
    private String rss;
    private String tty;
    private String stat;
    private String start;
    private String time;
    private String command;

}
