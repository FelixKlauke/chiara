package com.felixklauke.chiara.bukkit.user;

import com.google.common.base.Preconditions;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Singleton
public final class PermissionUserSessionFactory {
  private final Plugin plugin;

  @Inject
  private PermissionUserSessionFactory(Plugin plugin) {
    this.plugin = plugin;
  }

  public PermissionUserSession createSession(
    Player player,
    PermissionUser user
  ) {
    Preconditions.checkNotNull(player);
    Preconditions.checkNotNull(user);
    var permissionAttachment = player.addAttachment(plugin);
    return PermissionUserSession.of(player, user, permissionAttachment);
  }
}