package net.mfjassociates.bitbucket.plugins.hook;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookRequest;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.setting.Settings;
//import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

public class RejectTagHook implements PreRepositoryHook<RepositoryHookRequest>{
	private static final String DEFAULT_TAG_PREFIX = "MFJ";
	private static final String AUTHORIZED_USERS = "authorizedUsers";
	protected final AuthenticationContext authenticationContext;
//	private final I18nService i18nService;

	@Autowired
	public RejectTagHook(@ComponentImport AuthenticationContext anAuthenticationContext/*,@ComponentImport I18nService anI18nService*/) {
		this.authenticationContext = anAuthenticationContext;
//		this.i18nService=anI18nService;
	}

	/**
	 * Reject if a restricted tag is used and the user is not authorized
	 * @param hookRequest
	 * @param context
	 * @return a RepositoryHookResult to indicate whether to allow or not the push
	 */
	protected RepositoryHookResult detectIllegalTags(RepositoryHookRequest hookRequest, PreRepositoryHookContext context) {
		ApplicationUser user = authenticationContext.getCurrentUser();
		RepositoryHookResult result = RepositoryHookResult.accepted();
		String username = user != null ? user.getName() : "<unknown>";
		Settings settings = context.getSettings();
		String prefix1=settings.getString("prefix", DEFAULT_TAG_PREFIX);
		if (prefix1.isEmpty()) prefix1=DEFAULT_TAG_PREFIX;
		final String prefix=prefix1;
		String authorizedUsers1=settings.getString(AUTHORIZED_USERS, "admin");
		if (authorizedUsers1.isEmpty()) authorizedUsers1="admin";
		final Set<String> authorizedUsers=new HashSet<>(Arrays.asList(authorizedUsers1.split(","))); // remove duplicates
		List<String> illegalTags = hookRequest.getRefChanges().stream().map(change -> change.getRef().getId())
				.filter(id -> id.startsWith("refs/tags/" + prefix)).collect(Collectors.toList());
		boolean notAuthorized=authorizedUsers.stream().filter(u -> username.equals(u)).count()==0;
		if (illegalTags.size() > 0 && notAuthorized) {
			result = RepositoryHookResult.rejected("IllegalTagPush",
					MessageFormat.format("The following tags are reserved and cannot be pushed by username {0}: {1}",
							username, illegalTags.stream().collect(Collectors.joining(", "))));
		}
		return result;
	}

	@Override
	public RepositoryHookResult preUpdate(PreRepositoryHookContext context, RepositoryHookRequest request) {
		return detectIllegalTags(request, context);
	}
}
