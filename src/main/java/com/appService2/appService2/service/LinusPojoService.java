package com.appService2.appService2.service;

import com.appService2.appService2.entity.LinusPojo;

import java.io.IOException;
import java.util.List;

public interface LinusPojoService {

     List<LinusPojo> getAllRunningService();

     List<LinusPojo> getRunningServiceByPrefix(String prefixString);
     void notifyDownService() throws IOException;

}
