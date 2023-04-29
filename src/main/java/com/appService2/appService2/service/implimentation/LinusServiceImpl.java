package com.appService2.appService2.service.implimentation;

import com.appService2.appService2.entity.LinusPojo;
import com.appService2.appService2.service.LinusPojoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinusServiceImpl implements LinusPojoService {


    @Override
    public List<LinusPojo> getAllRunningService() {

        try {
            String command = "launchctl list";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));

//            BufferedReader stdError = new BufferedReader(new
//                    InputStreamReader(process.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s;
            List<LinusPojo> pojos = new ArrayList<>();
            int count = 0;
            while ((s = stdInput.readLine()) != null) {
                String[] outputList = s.split("");
                List<String> actualStrings = Arrays.stream(outputList)
                        .filter(str -> !str.isEmpty()).collect(Collectors.toList());
                System.out.println(s);

                if (0 == count++) continue;

                LinusPojo pojo = new LinusPojo();
                pojo.setUser(actualStrings.get(0));
                pojo.setPid(actualStrings.get(1));
                pojo.setCpu(actualStrings.get(2));
                pojo.setMem(actualStrings.get(3));
                pojo.setVsz(actualStrings.get(4));
                pojo.setRss(actualStrings.get(5));
                pojo.setTty(actualStrings.get(6));
                pojo.setStat(actualStrings.get(7));
                pojo.setStart(actualStrings.get(8));
                pojo.setTime(actualStrings.get(9));
                pojo.setCommand(actualStrings.get(10));

                pojos.add(pojo);

            }
            return pojos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public List<LinusPojo> getRunningServiceByPrefix(String prefix) {
        return getAllRunningService()
                .stream().filter(pojo -> pojo.getCommand().toLowerCase()
                        .contains(prefix.toLowerCase()))
                .peek(pojo -> log.info(pojo.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public void notifyDownService() throws IOException {
            String command = "launchctl list";
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));

//            System.out.println("Here is the standard output of the command:\n");
            String s;
            List<LinusPojo> pojos = new ArrayList<>();
            int count = 0;
            while ((s = stdInput.readLine()) != null) {
                String[] outputList = s.split("");
                List<String> actualStrings = Arrays.stream(outputList)
                        .filter(str -> !str.isEmpty()).collect(Collectors.toList());
//                System.out.println(s);

                if (0 == count++) continue;

                LinusPojo pojo = new LinusPojo();
                pojo.setUser(actualStrings.get(0));
                pojo.setPid(actualStrings.get(1));
                pojo.setCpu(actualStrings.get(2));
                pojo.setMem(actualStrings.get(3));
                pojo.setVsz(actualStrings.get(4));
                pojo.setRss(actualStrings.get(5));
                pojo.setTty(actualStrings.get(6));
                pojo.setStat(actualStrings.get(7));
                pojo.setStart(actualStrings.get(8));
                pojo.setTime(actualStrings.get(9));
                pojo.setCommand(actualStrings.get(10));

                pojos.add(pojo);

            }
        System.out.println("Pojo size.......... "+pojos.size());
        // Loop through services and check their status
        for (LinusPojo service : pojos) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("sudo", "launchctl", "list");
                Process process1 = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process1.getInputStream()));

                // Parse the output of the launchctl list command
                String line;
                boolean isRunning = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains((CharSequence) service)) {
                        isRunning = true;
                        String message = "Service "+ service +" is running";
                        System.out.println("Running "+message);
                        break;
                    }
                }

                if (!isRunning) {
                    // Service is not running, send an email alert
                    String message = "Service " + service + " is not running";
                    System.out.println("Not running "+message);
//                    sendEmail(to, from, subject, message, smtpServer, smtpPort, smtpUser, smtpPassword);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}


