package masr;

import java.util.List;

public final class Platform {

    private final PlatformConfig config;
    private final GeoService geo;
    private final RestaurantRepository restaurants;
    private final CustomerRepository customers;
    private final RiderRepository riders;
    private final OrderRepository orders;
    private final PromotionRegistry promotions;
    private final PricingEngine pricing;
    private final DispatchQueue dispatchQueue;
    private final AuditLog auditLog;
    private final CustomerNotifier notifier;
    private final RiderDashboardUpdater riderDashboard;
    private final AuditLogger auditLogger;
    private final StatisticsRecalculator statistics;
    private final OrderService orderService;
    private final ReportService reports;
    private final RestaurantSearchFacade search;
    private final IdGenerator idGenerator;

    public Platform() {
        this(PlatformConfig.get(), true);
    }

    public Platform(boolean withDemoData) {
        this(PlatformConfig.get(), withDemoData);
    }

    public Platform(PlatformConfig config) {
        this(config, true);
    }

    public Platform(PlatformConfig config, boolean withDemoData) {
        this.config = config;
        this.geo = GeoService.fromSpec(config.value("geo.distances", ""));
        this.restaurants = new RestaurantRepository();
        this.customers = new CustomerRepository();
        this.riders = new RiderRepository();
        this.orders = new OrderRepository();
        this.promotions = new PromotionRegistry();
        this.pricing = new PricingEngine(config, geo);
        this.dispatchQueue = new DispatchQueue();
        this.auditLog = new AuditLog();
        this.notifier = new CustomerNotifier(customers);
        this.riderDashboard = new RiderDashboardUpdater(riders);
        this.auditLogger = new AuditLogger(auditLog);
        this.statistics = new StatisticsRecalculator();
        this.idGenerator = new IdGenerator();
        List<OrderEventListener> listeners = List.of(notifier, riderDashboard, auditLogger, statistics);
        this.orderService = new OrderService(restaurants, customers, riders, orders, promotions, pricing,
                geo, dispatchQueue, listeners, idGenerator);
        this.reports = new ReportService(restaurants, customers, riders, orders);
        this.search = new RestaurantSearchFacade(restaurants, customers);
        if (withDemoData) {
            SeedData.install(this);
        }
    }

    public PlatformConfig config() {
        return config;
    }

    public GeoService geo() {
        return geo;
    }

    public RestaurantRepository restaurants() {
        return restaurants;
    }

    public CustomerRepository customers() {
        return customers;
    }

    public RiderRepository riders() {
        return riders;
    }

    public OrderRepository orders() {
        return orders;
    }

    public PromotionRegistry promotions() {
        return promotions;
    }

    public PricingEngine pricing() {
        return pricing;
    }

    public DispatchQueue dispatchQueue() {
        return dispatchQueue;
    }

    public AuditLog auditLog() {
        return auditLog;
    }

    public CustomerNotifier notifier() {
        return notifier;
    }

    public RiderDashboardUpdater riderDashboard() {
        return riderDashboard;
    }

    public AuditLogger auditLogger() {
        return auditLogger;
    }

    public StatisticsRecalculator statistics() {
        return statistics;
    }

    public OrderService orderService() {
        return orderService;
    }

    public ReportService reports() {
        return reports;
    }

    public RestaurantSearchFacade search() {
        return search;
    }

    public IdGenerator idGenerator() {
        return idGenerator;
    }

    public String nextId(String prefix) {
        return idGenerator.next(prefix);
    }
}
