package com.sunpc.buildnow.service;

import com.jcraft.jsch.JSch;
import com.sunpc.buildnow.config.EnvProperties;
import com.sunpc.buildnow.config.GitProperties;
import com.sunpc.buildnow.dao.IPromoteDao;
import com.sunpc.buildnow.util.git.*;
import org.eclipse.jgit.api.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.SocketException;
import java.util.*;

@Service
public class PromoteService implements IPromoteService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private EnvProperties envProperties;

    @Autowired
    private GitProperties gitProperties;

    @Autowired
    private PropertiesService propertiesSerivce;

    @Autowired
    private GitService gitService;

    @Autowired
    private FtpService ftpService;

    @Autowired
    private IPromoteDao promoteDao;

    private int stepSeq = 0;
    private String inFilePath = "C:/temp/ibuild/";
    private String gitRepositoryPath = "C:/temp/gitRepository/";
    private String promotion = "TFMS.F.DD.TQM.PROMOTE";

    @Override
    public Map<String, Object> promote(String requestID, String jobLinkNbr, String gitURI,
                                       String envCode, String envHost, String envPort, String envUserID, String envPassword,
                                       String promoteMember, String branch, String origin, String feature) {
        Map<String, Object> result = new HashMap<>();

        try {
            pushCode(requestID, promoteMember, branch, origin, feature);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
        } finally {
            return result;
        }
    }

    @Override
    public Map<String, Object> receiveReq(Map<String, Object> properties) {
        Map<String, Object> result = new HashMap<String, Object>();

        String userEmail = properties.get("userName").toString();
        String envCode = properties.get("envCode").toString();
        String promoteMember = properties.get("promoteMember").toString();
        String branch = properties.get("branch").toString();
        String origin = properties.get("origin").toString();
        String feature = properties.get("feature").toString();
        String gitURI = properties.get("gitURI").toString();
        String envHost = properties.get("envHost").toString();
        String envPort = properties.get("envPort").toString();
        String envUserID = properties.get("envUserID").toString();
        String envPassword = properties.get("envPassword").toString();

        String linkNbr = Integer.valueOf(getJobLinkNBR(envCode, origin)).toString();

        String gitPath = gitRepositoryPath + envCode + "-" + origin + "-" + linkNbr;

        logger.info("Set Git, git path is " + gitPath + ", git URI is " + gitURI);
        setGit(gitPath, gitURI);

        logger.info("Set environment, env name is " + envCode + ", env host is " + envHost + ", env port is " + envPort
                + ", env user id is " + envUserID);
        setEnv(envCode, envHost, Integer.parseInt(envPort), envUserID, envPassword);

        try {
            File file = new File(gitProperties.getPath() + "/.git");
            if (!file.exists()) {
                logger.info("create local repository " + gitProperties.getPath());
                gitService.create(gitProperties.getUri(), gitProperties.getPath());
            }

            gitService.open(gitProperties.getPath());

            if (gitService.branchExist(branch, true)) {
                result.put("status", "branchExist");
                return result;
            } else if (gitService.branchExist(branch, false)){
                gitService.removeLocalBranch(branch);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        promoteDao.insertRequest(userEmail, envCode, promoteMember, branch, origin, feature, linkNbr, "AVAILABLE");

        result.put("status", "success");

        return result;
    }

    @Override
    public Map<String, Object> getEnv(Map<String, Object> properties) {
        logger.info("get environment information");
        Map<String, Object> result;
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String envCode = properties.get("envCode").toString();
        String userName = properties.get("userName").toString();

        result = promoteDao.getUserInfo(envCode, userName).get(0);

        result.put("LOGON_USER_PWD", DesEncrypter.decrypt(result.get("LOGON_USER_PWD").toString().trim()));

        result.put("envHost", promoteDao.getEnvInfo(envCode, "Host").get(0).get("ENV_DEFAULT_VALUE").toString().trim());
        result.put("envPort", promoteDao.getEnvInfo(envCode, "SFTP Port").get(0).get("ENV_DEFAULT_VALUE").toString().trim());
        resultMap.put("result", result);


        return resultMap;
    }

    @Override
    public Map<String, Object> setEnv(Map<String, Object> properties) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String userEmail = properties.get("userEmail").toString();
        String envCode = properties.get("envCode").toString();
        String envUserID = properties.get("envUserID").toString();
        String envPassword = DesEncrypter.encrypt(properties.get("envPassword").toString());
        String gitURI = properties.get("gitURI").toString();

        promoteDao.saveUserInfo(userEmail, envCode, envUserID, envPassword, gitURI);

        resultMap.put("status", "success");
        return resultMap;
    }

    @Override
    public Map<String, Object> setGit(Map<String, Object> properties) {
        Map<String, Object> resultMap = new HashMap<>();

        String userEmail = properties.get("userEmail").toString();
        String envCode = properties.get("envCode").toString();
        String gitURI = properties.get("gitURI").toString();

        promoteDao.saveGit(userEmail, envCode, gitURI);

        resultMap.put("status", "success");
        return resultMap;
    }

    @Override
    public Map<String, Object> getReqInfo(Map<String, Object> properties) {
        return promoteDao.getReqInfo();
    }

    @Override
    public Map<String, Object> getCompInfo(Map<String, Object> properties) {
        return promoteDao.getCompInfo();
    }

    @Override
    public Map<String, Object> getFullList(Map<String, Object> properties) {
        logger.info("get full history list");
        Map<String, Object> resultMap = new HashMap<>();
        String envCode = properties.get("envCode").toString().trim();
        String origin = properties.get("histOrigin").toString().trim();
        String startDate = "";
        String endDate = "";
        String uuid = properties.get("uuid").toString().trim();
        String name = "FullHistList";
        if (properties.get("histStartDate") != null) {
            startDate = properties.get("histStartDate").toString().trim();
        }
        if (properties.get("histEndDate") != null) {
            endDate = properties.get("histEndDate").toString().trim();
        }

        String linkNbr = Integer.valueOf(getJobLinkNBR(envCode, origin)).toString();

        promoteDao.insertRequest("", "", "", "", "", "", linkNbr, "HISTORY");

        String pathStr = gitRepositoryPath + envCode + "-" + origin + "-" + linkNbr;

        String featureStr = properties.get("histFeature").toString().trim();

        try {
            Process process = Runtime.getRuntime().exec(prepareCommand(featureStr, startDate, endDate), (String[]) null, new File(pathStr));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s = "";
            String temp = "";
            String sep = ",";
            File targetFile = new File(inFilePath + uuid + File.separator + "HistList");
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            PrintWriter pw = new PrintWriter(new File(targetFile + File.separator + name + ".csv"));
            StringBuilder sb = new StringBuilder("File,Action,Author Name,Author Email,\tCommit Timestamp,Feature\n");

            while (true) {
                while ((s = stdInput.readLine()) != null) {
                    if (s.contains(",")) {
                        temp = s;
                    } else if (s.trim().length() > 0) {
                        String[] ss = s.split("\t");
                        if (ss.length == 2 && !"".equals(temp)) {
                            String[] temps = temp.split(",");
                            sb.append(ss[1] + sep + getStatus(ss[0]) + sep + temps[0] + sep + temps[1] + sep + temps[2] + sep + temps[3] + "\n");
                        } else {
                            temp = "";
                        }
                    }
                }
                pw.write(sb.toString());
                pw.close();

                logger.info("Completed to generate full history.");
                break;
            }
        } catch (IOException var16) {
            var16.printStackTrace();
        }

        resultMap.put("status", "success");


        return resultMap;
    }

    @Override
    public Map<String, Object> getUniqueList(Map<String, Object> properties, HttpServletRequest request) {
        logger.info("get unique history list");
        Map<String, Object> resultMap = new HashMap<>();
        String envCode = properties.get("envCode").toString().trim();
        String origin = properties.get("histOrigin").toString().trim();

        String linkNbr = Integer.valueOf(getJobLinkNBR(envCode, origin)).toString();

        String pathStr = gitRepositoryPath + envCode + "-" + origin + "-" + linkNbr;
        String featureStr = properties.get("histFeature").toString().trim();

        String startDate = "";
        String endDate = "";
        if (properties.get("histStartDate") != null) {
            startDate = properties.get("histStartDate").toString().trim();
        }
        if (properties.get("histEndDate") != null) {
            endDate = properties.get("histEndDate").toString().trim();
        }


        String uuid = properties.get("uuid").toString().trim();
        String name = "UniqueHistList";
        if (properties.get("histStartDate") != null) {
            startDate = properties.get("histStartDate").toString().trim();
        }
        if (properties.get("histEndDate") != null) {
            endDate = properties.get("histEndDate").toString().trim();
        }

        Set<String> list = new HashSet();
        TreeSet<String> nameList = new TreeSet();
        HashMap mapByname = new HashMap();

        try {
            Process process = Runtime.getRuntime().exec(prepareCommand(featureStr, startDate, endDate), (String[]) null, new File(pathStr));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s = "";
            String temp = "";
            String sep = ",";
            File targetFile = new File(inFilePath + uuid + File.separator + "HistList");
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            PrintWriter pw = new PrintWriter(new File(targetFile + File.separator + name + ".csv"));

            StringBuilder sb = new StringBuilder("File,Action,Author Name,Author Email,\tCommit Timestamp,Feature\n");

            while (true) {
                while ((s = stdInput.readLine()) != null) {
                    if (s.contains(",")) {
                        temp = s;
                    } else if (s.trim().length() > 0) {
                        String[] ss = s.split("\t");
                        if (ss.length == 2 && !"".equals(temp)) {
                            if (!"D".equalsIgnoreCase(ss[0]) && !ss[1].contains(".PROMOTE/") && !list.contains(ss[1])) {
                                String[] temps = temp.split(",");
                                sb.append(ss[1] + sep + getStatus(ss[0]) + sep + temps[0] + sep + temps[1] + sep + temps[2] + sep + temps[3] + "\n");
                                nameList.add(temps[0]);
                                if (mapByname.containsKey(temps[0])) {
                                    ((ArrayList) mapByname.get(temps[0])).add(genLine(ss[1]));
                                } else {
                                    ArrayList<String> tempList = new ArrayList();
                                    tempList.add(genLine(ss[1]));
                                    mapByname.put(temps[0], tempList);
                                }
                            }

                            list.add(ss[1]);
                        } else {
                            temp = "";
                        }
                    }
                }

                HttpSession session = request.getSession();
                session.setAttribute("mapByname", mapByname);
                session.setAttribute("nameList", nameList);
                pw.write(sb.toString());
                pw.close();

                logger.info("Completed to generate unique history.");

                break;
            }
        } catch (IOException var22) {
            var22.printStackTrace();
        }

        resultMap.put("status", "success");

        resultMap.put("nameList", nameList);
        resultMap.put("mapByname", mapByname);

        return resultMap;
    }

    @Override
    public Map<String, Object> uploadUniqueList(Map<String, Object> properties, HttpSession session) {
        logger.info("upload unique history list");
        Map<String, Object> resultMap = new HashMap<>();
        ArrayList<String> nameList = (ArrayList<String>) properties.get("nameList");
        HashMap mapByname = (HashMap) properties.get("mapByname");
        //String envCode = properties.get("envCode").toString().trim();
        String featureStr = properties.get("histFeature").toString().trim();
        String envHost = properties.get("envHost").toString().trim();
        int envPort = Integer.parseInt(properties.get("envPort").toString());
        String envUserID = properties.get("envUserID").toString().trim();
        String envPassword = properties.get("envPassword").toString().trim();
        String uuid = properties.get("uuid").toString().trim();

        try {
            generatePromotionFileAndUploadToZServer(envHost, envPort, envUserID, envPassword, featureStr, nameList, mapByname, uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }


        resultMap.put("status", "success");

        return resultMap;
    }

    @Override
    public Map<String, Object> handleStopRequest(Map<String, Object> properties) {
        logger.info("Stop the request");
        Map<String, Object> resultMap = new HashMap<>();
        String envCode = properties.get("envCode").toString().trim();
        String userName = properties.get("userName").toString().trim();
        //int envPort = Integer.parseInt(properties.get("envPort").toString());
        String feature = properties.get("feature").toString().trim();
        String promoteMember = properties.get("promoteMember").toString().trim();
        String branch = properties.get("branch").toString().trim();

        //promoteDao.updatePromoteStepStatus(envCode,userName,feature,promoteMember,branch,"ERRORING");


        resultMap.put("status", "success");

        return resultMap;
    }

    private void setGit(String repositoryPath, String repositoryUri) {
        if (!repositoryPath.equals("")) gitProperties.setPath(repositoryPath);
        if (!repositoryUri.equals("")) gitProperties.setUri(repositoryUri);

        try {
            propertiesSerivce.saveProperties(envProperties, gitProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setEnv(String name, String host, int port, String username, String password) {

        if (!name.equals("")) envProperties.setName(name);
        if (!host.equals("")) envProperties.setHost(host);
        if (port != 0) envProperties.setPort(port);
        if (!username.equals("")) envProperties.setUsername(username);
        if (!password.equals("")) envProperties.setPassword(password);

        try {
            propertiesSerivce.saveProperties(envProperties, gitProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pushCode(String requestID, String promoteMember, String branch, String origin, String feature) {
        try {
            ArrayList<String> libList = new ArrayList<>();
            libList.add("RUN");
            libList.add("RUNT");
            libList.add("PROCLIB");
            libList.add("PROCT");
            libList.add("INSTJCL");
            libList.add("INSTT");
            libList.add("PLI");
            // Set JSch configuration
            JSch.setConfig("StrictHostKeyChecking", "no");

            // Step 0: Starting
            logger.info("Push command started");

            stepSeq = 1;

            moveStep(requestID, stepSeq, true, false, true);

            // Step 0: Clone
            File file = new File(gitProperties.getPath() + "/.git");
            if (!file.exists()) {
                logger.info("create local repository " + gitProperties.getPath());
                gitService.create(gitProperties.getUri(), gitProperties.getPath());
            }

            // Step 1: Connect to local repository
            stepSeq = 2;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Connecting to local repository");
            gitService.open(gitProperties.getPath());

            // Step 2: Check out master
            stepSeq = 3;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Checking out Branch " + origin);
            try {
                gitService.checkout(origin);
            }catch(Exception e){
                logger.info("reset the repository, then try to checkout "+origin+" again");
                gitService.reset();
                gitService.checkout(origin);
            }

            // Step 3: Pull master
            stepSeq = 4;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Pulling Branch " + origin + " (This may take a few minutes)");
            gitService.pull();
            logger.info("Pulled " + origin);

            // Step 4: Check out branch
            stepSeq = 5;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Checking out Branch " + branch);
            try{
                gitService.checkout(branch);
            }catch(Exception e){
                logger.info("reset the repository, then try to checkout "+branch+" again");
                gitService.reset();
                gitService.checkout(branch);
            }

            // Step 5: Connect to z/OS
            stepSeq = 6;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Connecting to " + envProperties.getName());
            ftpService.connect(envProperties.getHost(), envProperties.getPort(),
                    envProperties.getUsername(), envProperties.getPassword());

            // Step 6: Download TFMS.DD.PROMOTE
            stepSeq = 7;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Downloading " + promoteMember);
            String remotePromoteName = "'TFMS.DD.PROMOTE(" + promoteMember + ")'";
            String localPromoteName = gitProperties.getPath() + "/TFMS.DD.PROMOTE/" + promoteMember;
            ftpService.download(remotePromoteName, localPromoteName);
            logger.info("Downloaded to " + localPromoteName);

            // Step 7: Download all members
            stepSeq = 8;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Downloading all members in " + promoteMember);
            List<Map<String, Object>> listMember = ZosUtil.readPromoteMember(localPromoteName);
            for (Map<String, Object> map : listMember) {
                String remoteName = "'TFMS.DD." + map.get("lib").toString() + "(" + map.get("name").toString() + ")'";
                String localName = gitProperties.getPath() + "/TFMS.DD." + map.get("lib").toString() + "/" + map.get("localName").toString();
                logger.info("Downloading " + remoteName + " to " + localName);
                ftpService.download(remoteName, localName);
                if (libList.contains(map.get("lib").toString().toUpperCase())) {
                    ZosUtil.formatPLI(localName);
                }

                if("PLI".equals(map.get("lib").toString())){
                    remoteName = "'TFMS.DD.LINK(" + map.get("name").toString() + ")'";
                    localName = gitProperties.getPath() + "/TFMS.DD.LINK/" + map.get("localName").toString();
                    logger.info("Downloading " + remoteName + " to " + localName);
                    ftpService.download(remoteName, localName);
                    if (libList.contains(map.get("lib").toString().toUpperCase())) {
                        ZosUtil.formatPLI(localName);
                    }
                }


            }
            logger.info("Download finished");

            // Step 8: Close FTP
            stepSeq = 9;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Disconnecting from " + envProperties.getName());
            ftpService.disconnect();

            // Step 9: Commit branch
            stepSeq = 10;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Committing Branch " + branch);
            logger.info(gitService.commit("Feature " + feature));

            // Step 10: Push branch
            stepSeq = 11;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Pushing Branch " + branch + " (This may take a few minutes)");
            gitService.push();
            logger.info("Pushed to " + branch);

            // Step X: Close GIT
            stepSeq = 12;
            moveStep(requestID, stepSeq, false, false, true);
            logger.info("Closing local repository");
            gitService.close();

            // Step Z: Finish
            stepSeq = 13;
            moveStep(requestID, stepSeq, false, true, true);
            logger.info("Push command finished");


        } catch (IOException e) {
            e.printStackTrace();
            moveStep(requestID, stepSeq, false, false, false);
        } catch (RefAlreadyExistsException e) {
            e.printStackTrace();
            moveStep(requestID, stepSeq, false, false, false);
        } catch (RefNotFoundException e) {
            e.printStackTrace();
            moveStep(requestID, stepSeq, false, false, false);

        } catch (InvalidRefNameException e) {
            e.printStackTrace();
            moveStep(requestID, stepSeq, false, false, false);
        } catch (CheckoutConflictException e) {
            e.printStackTrace();
            moveStep(requestID, stepSeq, false, false, false);
        } catch (GitAPIException e) {
            e.printStackTrace();
            moveStep(requestID, stepSeq, false, false, false);
        }
    }

    private int getJobLinkNBR(String envCode, String origin) {
        List<Map<String, Object>> busyLink = promoteDao.getBusyLink(envCode, origin);

        if (busyLink.size() == 10) {
            return 0;
        } else if (busyLink.size() == 0) {
            return 1;
        } else if (busyLink.size() > 0 && busyLink.size() < 10) {
            for (int i = 1; i <= 10; i++) {
                for (int j = 0; j < busyLink.size(); j++) {
                    if (i == Integer.parseInt(busyLink.get(j).get("JOB_LINK_NBR").toString().trim())) {
                        break;
                    } else {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    private void moveStep(String requestID, int stepSeq, boolean isFirstStep, boolean isLastStep, boolean complete) {
        if (complete) {
            if (isFirstStep) {
                //logger.info("This is the first step for request " + requestID + ", insert step info into PROOMOTE_STEP");
                promoteDao.insertPromoteStep(requestID, Integer.valueOf(stepSeq).toString());
            } else if (isLastStep) {
                //logger.info("The last step completed successfully, update it to complete");
                promoteDao.updatePromoteStepStatus(requestID, Integer.valueOf(stepSeq - 1).toString(), "COMPLETE");

                //logger.info("Request " + requestID + " completed.");
                promoteDao.updateStatusFromPromoteReq(requestID, "COMPLETE", null);
            } else {
                //logger.info("Step" + (stepSeq - 1) + " for request " + requestID + " completed, update it to complete");
                promoteDao.updatePromoteStepStatus(requestID, Integer.valueOf(stepSeq - 1).toString(), "COMPLETE");

                //logger.info("Start step" + stepSeq + " for " + requestID + ", insert step info into PROOMOTE_STEP");
                promoteDao.insertPromoteStep(requestID, Integer.valueOf(stepSeq).toString());
            }
        } else {
            logger.info("step" + stepSeq + " failed");
            promoteDao.updatePromoteStepStatus(requestID, Integer.valueOf(stepSeq).toString(), "ERROR");
            promoteDao.updateStatusFromPromoteReq(requestID, "ERROR", null);
        }
    }


    /*
      Git History

     */
    private String[] prepareCommand(String feature, String startDate, String endDate) {
        String[] precmd = getSysCommandPrefix();
        String cmd = "git log --pretty=format:\"%an,%ae,%cd,%s\" --name-status";
        String[] cmds;
        if (!"".equals(feature)) {
            cmds = feature.split(" ");
            String[] var10 = cmds;
            int var9 = cmds.length;

            for (int var8 = 0; var8 < var9; ++var8) {
                String f = var10[var8];
                cmd = cmd + " --grep=" + f;
            }
        }

        if (startDate != null && startDate != "") {
            cmd = cmd + " --since=\"" + startDate + "\"";
        }

        if (endDate != null && endDate != "") {
            cmd = cmd + " --until=\"" + endDate + "\"";
        }

        cmds = new String[]{precmd[0], precmd[1], cmd};
        return cmds;
    }

    private String[] getSysCommandPrefix() {
        String[] precmd = new String[]{"", ""};
        if (System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
            precmd[0] = "/bin/bash";
            precmd[1] = "-c";
        } else {
            precmd[0] = "cmd.exe";
            precmd[1] = "/c";
        }

        return precmd;
    }

    private String getStatus(String s) {
        switch (s.hashCode()) {
            case 65:
                if (s.equals("A")) {
                    return "Add";
                }
                break;
            case 68:
                if (s.equals("D")) {
                    return "Delete";
                }
                break;
            case 77:
                if (s.equals("M")) {
                    return "Modify";
                }
        }

        return "N/A";
    }

    private String genLine(String fileFullName) {
        String fileName = getFileNameForZ(fileFullName);
        String sep1 = getSep(fileName, 11);
        String folderName = getFolderNameForZ(fileFullName);
        String sep2 = getSep(folderName, 10);
        return fileName + sep1 + folderName + sep2 + "NMLMBR\n";
    }

    private String getFileNameForZ(String inName) {
        String name = inName;
        int exp_idx = inName.lastIndexOf(".");
        int sep_idx = inName.lastIndexOf("/");
        if (exp_idx > sep_idx) {
            name = inName.substring(0, exp_idx);
        }

        name = name.substring(sep_idx + 1);
        if (inName.contains(".RUN/") || inName.contains(".INSTJCL/")) {
            name = "FMS" + name.substring(3);
        }

        return name;
    }

    private String getSep(String content, int maxSize) {
        return maxSize > content.length() ? String.join("", Collections.nCopies(maxSize - content.length(), " ")) : "";
    }

    private String getFolderNameForZ(String inName) {
        String name = inName.split("/")[0];
        return name.substring(name.lastIndexOf(".") + 1);
    }

    private void generatePromotionFileAndUploadToZServer(String envHost, int port, String userID, String password, String featureStr, ArrayList<String> nameList, HashMap<String, ArrayList<String>> mapByname, String uuid) throws SocketException, IOException {
        String promotionFile = generateFileForPrompt(featureStr, nameList, mapByname, uuid);
        ftpService.connect(envHost, port, userID, password);
        String remoteName = "'" + promotion + "(" + featureStr + ")'";
        ftpService.upload(remoteName, promotionFile);
        ftpService.disconnect();
    }


    private String generateFileForPrompt(String feature, ArrayList<String> nameList, HashMap<String, ArrayList<String>> mapByname, String uuid) {
        StringBuilder sb = new StringBuilder("*" + feature + "\n*\n*MEMBER*   *TYPE*    *MEMTYPE*\n*\n");
        Iterator var6 = nameList.iterator();

        String target;
        while (var6.hasNext()) {
            target = (String) var6.next();
            sb.append("*" + target + "\n");
            Iterator var8 = ((ArrayList) mapByname.get(target)).iterator();

            while (var8.hasNext()) {
                String line = (String) var8.next();
                sb.append(line);
            }
        }

        target = inFilePath + uuid;

        try {
            File targetFile = new File(target);
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            PrintWriter pw = new PrintWriter(new File(target + File.separator + feature));
            pw.write(sb.toString());
            pw.close();
        } catch (FileNotFoundException var9) {
            var9.printStackTrace();
        }

        return target + File.separator + feature;
    }

    @Override
    public Map<String, Object> getFeatures(String envCode) {
        return promoteDao.getFeature(envCode);
    }


    @Override
    public Map<String, Object> addFeature(Map<String, Object> properties) {
        String envCode = (String) properties.get("envCode");
        String featureName = (String) properties.get("featureName");
        String featureDesc = (String) properties.get("featureDesc");
        int lastActUserId = (int) properties.get("lastActUserId");

        return promoteDao.addFeature(envCode, featureName, featureDesc,  lastActUserId);
    }


    @Override
    public Map<String, Object> updateFeatureStatus(Map<String, Object> properties) {
        String envCode = (String) properties.get("envCode");
        String featureName = (String) properties.get("featureName");
        String status = (String) properties.get("apprStatus");
        int lastActUserId = (int) properties.get("lastActUserId");

        return promoteDao.updateFeatureStatus(envCode, featureName, status,  lastActUserId);
    }

    @Override
    public Map<String, Object> getApproveFeatures(String envCode) {
        return promoteDao.getStatusFeature(envCode,"Approved");
    }

}
