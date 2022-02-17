package net.mfjassociates.bitbucket.plugins.hook;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.event.tag.TagCreationHookRequest;
import com.atlassian.bitbucket.event.tag.TagDeletionHookRequest;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookRequest;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.user.ApplicationUser;

public class RejectTagHook implements PreRepositoryHook<RepositoryHookRequest>{
	private static final String DEFAULT_TAG_PREFIX = "MFJ";
	protected final AuthenticationContext authenticationContext;

	public RejectTagHook(AuthenticationContext anAuthenticationContext) {
		this.authenticationContext = anAuthenticationContext;
	}

	protected RepositoryHookResult detectIllegalTags(RepositoryHookRequest hookRequest) {
		ApplicationUser user = authenticationContext.getCurrentUser();
		RepositoryHookResult result = RepositoryHookResult.accepted();
		String username = user != null ? user.getName() : "<unknown>";
		List<String> illegalTags = hookRequest.getRefChanges().stream().map(change -> change.getRef().getId())
				.filter(id -> id.startsWith("refs/tags/" + DEFAULT_TAG_PREFIX)).collect(Collectors.toList());
		if (illegalTags.size() > 0) {
			result = RepositoryHookResult.rejected("IllegalTagPush",
					MessageFormat.format("The following tags are reserved and cannot be pushed by username {0}: {1}",
							username, illegalTags.stream().collect(Collectors.joining(", "))));
		}
		return result;
	}

	@Override
	public RepositoryHookResult preUpdate(PreRepositoryHookContext context, RepositoryHookRequest request) {
		return detectIllegalTags(request);
	}
}
