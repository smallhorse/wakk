package com.ubtrobot.analytics;

public class AnalyticsDelegate implements Analytics {

    private Analytics mAnalyticsService;

    public AnalyticsDelegate(Class<Analytics> analyticsClass) {
        if (analyticsClass == null) {
            throw new IllegalArgumentException("AnalyticsClass is not null.");
        }

        try {
            mAnalyticsService = analyticsClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("AnalyticsClass is abstract class or interface.");
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("AnalyticsClass constructors is private.");
        }
    }

    @Override
    public void enable(boolean enable) {
        mAnalyticsService.enable(enable);
    }

    @Override
    public void setStrategy(Strategy strategy) {
        mAnalyticsService.setStrategy(strategy);
    }

    @Override
    public Strategy getStrategy() {
        return mAnalyticsService.getStrategy();
    }

    @Override
    public void recordEvent(Event event) {
        mAnalyticsService.recordEvent(event);
    }
}