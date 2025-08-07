package de.hybris.platform.opfacceleratoraddon.handler;

import de.hybris.platform.acceleratorstorefrontcommons.interceptors.BeforeViewHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import de.hybris.platform.controllers.OpfacceleratoraddonControllerConstants;
import org.springframework.web.servlet.ModelAndView;

public class OPFAcceleratorAddonBeforeViewHandler implements BeforeViewHandler {

    public static final String VIEW_NAME_MAP_KEY = "viewName";
    private Map<String, Map<String, String>> viewMap;

    @Override
    public void beforeView(final HttpServletRequest request, final HttpServletResponse response, final ModelAndView modelAndView) {
        final String viewName = modelAndView.getViewName();
        if (viewMap.containsKey(viewName)) {
            modelAndView.setViewName(OpfacceleratoraddonControllerConstants.ADDON_PREFIX + viewMap.get(viewName).get(VIEW_NAME_MAP_KEY));
        }
    }

    public Map<String, Map<String, String>> getViewMap() {
        return viewMap;
    }

    public void setViewMap(final Map<String, Map<String, String>> viewMap) {
        this.viewMap = viewMap;
    }
}
