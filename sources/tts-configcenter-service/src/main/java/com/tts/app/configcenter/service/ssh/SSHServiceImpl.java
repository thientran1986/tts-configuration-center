package com.tts.app.configcenter.service.ssh;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.ops4j.pax.cdi.api.OsgiService;

import com.tts.app.configcenter.model.server.Server;
import com.tts.app.configcenter.model.server.ServerDao;
import com.tts.app.configcenter.model.ssh.SSHCommand;
import com.tts.app.configcenter.model.ssh.SSHFeature;
import com.tts.app.configcenter.service.ssh.cmd.UICommand;
import com.tts.app.configcenter.service.ssh.common.HelloWorldFeature;
import com.tts.app.configcenter.service.ssh.common.PingCommand;
import com.tts.app.configcenter.service.ssh.common.SoftwareFeature;
import com.tts.app.configcenter.service.ssh.docker.DockerComposeFeature;
import com.tts.app.configcenter.service.ssh.docker.DockerFeature;
import com.tts.app.configcenter.service.ssh.tmaso.TMASmartOfficeFeature;

@Named
public class SSHServiceImpl implements SSHService {
    
    @OsgiService @Inject
    ServerDao serverDao;
    
    @Inject
    SSHCommandExecutor commandExecutor;
    
    
    Map<SSHFeature, SoftwareFeature> features = new LinkedHashMap<>();
    
    @PostConstruct
    public void init() {
        addFeature(new DockerFeature(commandExecutor));
        addFeature(new DockerComposeFeature(commandExecutor));
        addFeature(new HelloWorldFeature(commandExecutor));
        addFeature(new TMASmartOfficeFeature(commandExecutor));
    }
    
    @Override
    public void addFeature(SoftwareFeature feature) {
        features.put(feature.getFeatureInfo(), feature);
    }

    @Override
    public List<SoftwareFeature> getSupportedFeatures() {
        return new ArrayList<>(features.values());
    }

    @Override
    public boolean ping(String srIpAddress, String desIpAddress) throws Exception {
        Server server = serverDao.findByServerIP(srIpAddress);
        SSHResult rs = new PingCommand(desIpAddress).execute(commandExecutor, server);
        return rs.getExistStatus() == SSHResult.PING_REACHABLE;
    }

    @Override
    public Map<SoftwareFeature, Boolean> checkFeatureStatus(String ipAddress) throws Exception {
        Server server = serverDao.findByServerIP(ipAddress);
        
        Map<SoftwareFeature, Boolean> rs = new LinkedHashMap<>();
        for (SoftwareFeature feature : features.values()) {
            boolean status = false;
            try {
                status = feature.check(server);
            } catch (Exception e) {
                e.printStackTrace();
            }
            rs.put(feature, status);
        }
        return rs;
    }
    
    @Override
    public SSHResult executeCommand(String ipAddress, SSHCommand command) throws Exception {
        Server server = serverDao.findByServerIP(ipAddress);
        
        SoftwareFeature sshFeature = features.get(command.getFeature());
        Map<String, UICommand> uiCommands = sshFeature.getUICommands();
        UICommand uiCommand = uiCommands.get(command.getName());
        
        return uiCommand.execute(commandExecutor, server);
    }

    @Override
    public SSHResult installFeature(String ipAddress, SSHFeature feature) throws Exception {
        Server server = serverDao.findByServerIP(ipAddress);
        
        SoftwareFeature sshFeature = features.get(feature);
        return sshFeature.install(server);
    }

    @Override
    public SSHResult uninstallFeature(String ipAddress, SSHFeature feature, boolean removeDependency) throws Exception {
        Server server = serverDao.findByServerIP(ipAddress);
        
        SoftwareFeature sshFeature = features.get(feature);
        return sshFeature.uninstall(server, removeDependency);
    }
    
    @Override
    public String getLog(String ipAddress) {
        return commandExecutor.getLog(ipAddress);
    }
}
