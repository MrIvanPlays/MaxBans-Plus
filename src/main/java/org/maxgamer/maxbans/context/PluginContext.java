package org.maxgamer.maxbans.context;

import org.bukkit.Server;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.maxgamer.maxbans.config.JdbcConfig;
import org.maxgamer.maxbans.config.PluginConfig;
import org.maxgamer.maxbans.config.WarningConfig;
import org.maxgamer.maxbans.orm.HibernateConfigurer;
import org.maxgamer.maxbans.repository.*;
import org.maxgamer.maxbans.service.*;
import org.maxgamer.maxbans.transaction.Transactor;

/**
 * @author Dirk Jamieson <dirk@redeye.co>
 */
public class PluginContext {
    private PluginConfig config;
    private Server server;

    private SessionFactory sessionFactory;
    private Transactor transactor;
    private UserRepository userRepository;
    private BanRepository banRepository;
    private MuteRepository muteRepository;
    private AddressRepository addressRepository;
    private WarningRepository warningRepository;

    private UserService userService;
    private BroadcastService broadcastService;
    private LocatorService locatorService;
    private AddressService addressService;
    private WarningService warningService;
    
    public PluginContext(PluginConfig config, Server server) {
        this.config = config;
        this.server = server;

        JdbcConfig jdbc = config.getJdbcConfig();
        WarningConfig warnings = config.getWarningConfig();
        Configuration hibernate = HibernateConfigurer.configuration(jdbc);

        sessionFactory = hibernate.buildSessionFactory();
        transactor = new Transactor(sessionFactory);

        userRepository = new UserRepository(transactor);
        banRepository = new BanRepository(transactor);
        muteRepository = new MuteRepository(transactor);
        addressRepository = new AddressRepository(transactor);
        warningRepository = new WarningRepository(transactor);

        broadcastService = new BroadcastService(server);
        userService = new UserService(config, userRepository, banRepository, muteRepository);
        locatorService = new LocatorService(server, userService);
        addressService = new AddressService(addressRepository);
        warningService = new WarningService(server, warningRepository, broadcastService, locatorService, warnings);
    }

    public PluginConfig getConfig() {
        return config;
    }

    public Server getServer() {
        return server;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public BanRepository getBanRepository() {
        return banRepository;
    }

    public MuteRepository getMuteRepository() {
        return muteRepository;
    }

    public UserService getUserService() {
        return userService;
    }

    public BroadcastService getBroadcastService() {
        return broadcastService;
    }

    public Transactor getTransactor() {
        return transactor;
    }

    public LocatorService getLocatorService() {
        return locatorService;
    }

    public AddressRepository getAddressRepository() {
        return addressRepository;
    }

    public AddressService getAddressService() {
        return addressService;
    }

    public WarningService getWarningService() {
        return warningService;
    }

    public WarningRepository getWarningRepository() {
        return warningRepository;
    }
}
