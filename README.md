3. Customizing the Authorization Request:
Add Custom Parameters: To add custom parameters to the authorization request, you can implement the OAuth2AuthorizationRequestResolver interface:

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    @Override
    public AuthorizationRequest resolve(HttpServletRequest request) {
        // ... add custom parameters to the request
    }
}
4. Customizing the Token Response:
Handle Custom Token Claims: To handle custom claims in the token response, you can implement the OAuth2AccessTokenResponseClient interface:

@Component
public class CustomAccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        // ... handle custom claims in the response
    }
}

5. Customizing the User Info Endpoint:
Retrieve Custom Attributes: To retrieve custom attributes from the user info endpoint, you can implement the OAuth2UserService interface:

@Component
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // ... retrieve custom attributes from the user info endpoint
    }
}
For further customization, you can explore:
Using Spring Security's ClientRegistrationRepository to manage client registrations programmatically.
Using Spring Security's OAuth2AuthorizedClientService to manage authorized clients and their access tokens.
Using Spring Security's OAuth2AuthenticationSuccessHandler to customize the behavior after successful authentication.

3. Access the Access Token:
You can use the OAuth2AuthorizedClientManager to get an access token for your client:
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    public String getAccessToken() {
        OAuth2AuthorizedClientManager clientManager = OAuth2AuthorizedClientManagerBuilder.builder()
                .clientRegistrationRepository(clientRegistrationRepository)
                .build();

        OAuth2AuthorizedClient client = clientManager.authorize(
                OAuth2AuthorizationContext.builder()
                        .registrationId("my-client")
                        .build()
        );

        return client.getAccessToken().getTokenValue();
    }
}

4. Make Authenticated Requests:
Use the access token to make authenticated requests to the resource server:
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MyService {

    @Autowired
    private MyService myService;

    public String callProtectedResource() {
        String accessToken = myService.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // ... make your request using the RestTemplate
    }
}
Customizing OAuth2 refreshing in Spring Security can be achieved in a few ways, depending on what you want to achieve. Here are a few common scenarios:
1. Customizing the Refresh Token Request/Response:
Using DefaultRefreshTokenTokenResponseClient:
This is the default implementation for refreshing tokens. You can customize it by:
Pre-processing the token request: Override the preprocess() method to modify the request before it's sent to the authorization server.
Post-processing the token response: Override the postProcess() method to handle the response after receiving it from the authorization server.
Creating a custom OAuth2AccessTokenResponseClient:
If you need more control, you can implement your own OAuth2AccessTokenResponseClient. This allows you to fully customize the request and response handling for the refresh token grant.
Example (customizing DefaultRefreshTokenTokenResponseClient):

Example (customizing DefaultRefreshTokenTokenResponseClient):
Java

@Configuration
public class SecurityConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .refreshToken(refreshTokenGrantBuilder ->
                                refreshTokenGrantBuilder.accessTokenResponseClient(customRefreshTokenTokenResponseClient()))
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    private OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> customRefreshTokenTokenResponseClient() {
        DefaultRefreshTokenTokenResponseClient client = new DefaultRefreshTokenTokenResponseClient();
        client.setRequestEntityConverter(new CustomRefreshTokenGrantRequestEntityConverter());
        return client;
    }
}
2. Adjusting Refresh Token Expiry:
In the Authorization Server:
If you control the authorization server, you can modify the expiry time of refresh tokens.
In Spring Security:
You can't directly adjust the refresh token expiry in Spring Security, but you can implement logic to handle the expiration and re-authentication if needed.
3. Customizing the Token Storage:
By default, Spring Security stores tokens in memory. You can customize this by implementing your own OAuth2AuthorizedClientService to use a different storage mechanism, such as a database or a distributed cache.
4. Manually Refreshing Tokens:
You can manually refresh tokens using the OAuth2AuthorizedClientService and OAuth2AuthorizedClientManager. This gives you full control over when and how tokens are refreshed.
Example (manually refreshing a token):

Example (manually refreshing a token):
Java

@Autowired
private OAuth2AuthorizedClientService authorizedClientService;

@Autowired
private OAuth2AuthorizedClientManager authorizedClientManager;

public void refreshAccessToken(String clientRegistrationId) {
    OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, "user");

    if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
        OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest =
                OAuth2RefreshTokenGrantRequest.builder()
                        .clientId(authorizedClient.getClientRegistration().getClientId())
                        .clientSecret(authorizedClient.getClientRegistration().getClientSecret())
                        .refreshToken(authorizedClient.getRefreshToken().getTokenValue())
                        .build();

        OAuth2AuthorizedClient refreshedClient = authorizedClientManager.authorize(refreshTokenGrantRequest);
        authorizedClientService.saveAuthorizedClient(refreshedClient, "user");
    }
}

@Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizationCodeGrantRequestEntityConverter requestEntityConverter =
                new OAuth2AuthorizationCodeGrantRequestEntityConverter();
        requestEntityConverter.addParametersConverter(parametersConverter());

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .refreshToken(refreshTokenGrantBuilder ->
                                refreshTokenGrantBuilder.accessTokenResponseClient(customRefreshTokenTokenResponseClient()))
                        .build();
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
                new DefaultAuthorizationCodeTokenResponseClient();
        accessTokenResponseClient.setRequestEntityConverter(requestEntityConverter);


        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, (OAuth2AuthorizedClientRepository) authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    private OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> customRefreshTokenTokenResponseClient() {
        DefaultRefreshTokenTokenResponseClient client = new DefaultRefreshTokenTokenResponseClient();
        client.setRequestEntityConverter(new CustomRefreshTokenGrantRequestEntityConverter());
        return client;
    }

    
    public void refreshAccessToken(String clientRegistrationId) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, "user");

        if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
            OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest = new OAuth2RefreshTokenGrantRequest(
                    authorizedClient.getClientRegistration(),authorizedClient.getAccessToken(),
                    authorizedClient.getRefreshToken(),authorizedClient.getAccessToken().getScopes()
                    );


            OAuth2AuthorizedClient refreshedClient = authorizedClientManager.authorize(refreshTokenGrantRequest);
            authorizedClientService.saveAuthorizedClient(refreshedClient,"bbbb");
        }
}
    private static Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> parametersConverter() {
        return (grantRequest) -> {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.set("audience", "xyz_value");

            return parameters;
        };
    }

@Configuration
public class SecurityConfig {

	@Bean
	public DefaultAuthorizationCodeTokenResponseClient authorizationCodeAccessTokenResponseClient() {
		OAuth2AuthorizationCodeGrantRequestEntityConverter requestEntityConverter =
			new OAuth2AuthorizationCodeGrantRequestEntityConverter();
		requestEntityConverter.addParametersConverter(parametersConverter());

		DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
			new DefaultAuthorizationCodeTokenResponseClient();
		accessTokenResponseClient.setRequestEntityConverter(requestEntityConverter);

		return accessTokenResponseClient;
	}

	private static Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> parametersConverter() {
		// ...
	}

}    

