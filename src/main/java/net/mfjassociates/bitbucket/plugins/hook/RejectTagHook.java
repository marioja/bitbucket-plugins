package net.mfjassociates.bitbucket.plugins.hook;

import com.atlassian.bitbucket.hook.*;
import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.repository.*;

import java.util.Collection;

public class RejectTagHook implements PreReceiveRepositoryHook
{
    /**
     * Disables deletion of branches
     */
    @Override
    public boolean onReceive(RepositoryHookContext context, Collection<RefChange> refChanges, HookResponse hookResponse)
    {
        for (RefChange refChange : refChanges)
        {
            if (refChange.getType() == RefChangeType.DELETE)
            {
                hookResponse.err().println("The ref '" + refChange.getRef().getId() + "' cannot be deleted.");
                return false;
            }
        }
        return true;
    }
}
