package com.micronaut.app.security;

import io.micronaut.context.BeanContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.oauth2.ProviderResolver;
import io.micronaut.security.oauth2.endpoint.endsession.request.EndSessionEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Controller("/api")
public class ApiLogoutController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiLogoutController.class);
    private final ProviderResolver providerResolver;
    private final BeanContext beanContext;

    /**
     * @param providerResolver The provider resolver
     * @param beanContext The bean context
     */
    public ApiLogoutController(ProviderResolver providerResolver, BeanContext beanContext) {
        this.providerResolver = providerResolver;
        this.beanContext = beanContext;
    }
    /**
     *
     * @param request The current request
     * @param authentication The current authentication
     * @return {@link Logout} POJO encapsulating logout url
     */
    @Post("/logout")
    public HttpResponse<Logout> logout(HttpRequest request, Authentication authentication) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Received /api/logout request for user [{}]", authentication.getName());
        }
        Optional<String> logutUrl = providerResolver.resolveProvider(authentication)
            .flatMap(p -> beanContext.findBean(EndSessionEndpoint.class, Qualifiers.byName(p)))
            .map(endSessionEndpoint -> endSessionEndpoint.getUrl(request, authentication));

        if (!logutUrl.isPresent()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not resolve logout url");
            }
            return HttpResponse.unprocessableEntity();
        }
        Logout logout = new Logout();
        logout.setLogoutUrl(logutUrl.get());
        return HttpResponse.ok(logout);
    }
}
