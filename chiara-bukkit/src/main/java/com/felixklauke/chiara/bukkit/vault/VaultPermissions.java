package com.felixklauke.chiara.bukkit.vault;

import com.felixklauke.chiara.bukkit.group.PermissionGroup;
import com.felixklauke.chiara.bukkit.group.PermissionGroupRepository;
import com.felixklauke.chiara.bukkit.permission.PermissionStatus;
import com.felixklauke.chiara.bukkit.user.PermissionUserNotFoundException;
import com.felixklauke.chiara.bukkit.user.PermissionUserRepository;
import java.util.Arrays;
import java.util.UUID;
import javax.inject.Inject;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.Plugin;

public final class VaultPermissions extends Permission {
  private final Plugin plugin;
  private final PermissionGroupRepository groupRepository;
  private final PermissionUserRepository userRepository;

  @Inject
  private VaultPermissions(
    Plugin plugin,
    PermissionGroupRepository groupRepository,
    PermissionUserRepository userRepository
  ) {
    this.plugin = plugin;
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  @Override
  public String getName() {
    return plugin.getName();
  }

  @Override
  public boolean isEnabled() {
    return plugin.isEnabled();
  }

  private static final boolean SUPER_PERMS_SUPPORT = true;

  @Override
  public boolean hasSuperPermsCompat() {
    return SUPER_PERMS_SUPPORT;
  }

  @Override
  public boolean playerHas(String world, String player, String permission) {
    var userId = findPlayerUniqueId(player);
    if (world == null) {
      return playerHas(userId, permission);
    }
    return playerHasWorld(userId, permission, world);
  }

  private boolean playerHas(UUID userId, String permission) {
    var userOptional = userRepository.findUser(userId);
    return userOptional
      .map(permissionUser -> permissionUser.hasPermissions(permission))
      .orElse(false);
  }

  private boolean playerHasWorld(UUID userId, String permission, String world) {
    var userOptional = userRepository.findUser(userId);
    return userOptional
      .map(permissionUser -> permissionUser.hasPermissions(permission, world))
      .orElse(false);
  }

  @Override
  public boolean playerAdd(String world, String player, String permission) {
    if (world == null) {
      return setPlayerPermission(player, permission, PermissionStatus.ALLOWED);
    }
    return setPlayerWorldPermission(player, permission, world, PermissionStatus.ALLOWED);
  }

  @Override
  public boolean playerRemove(String world, String player, String permission) {
    if (world == null) {
      return setPlayerPermission(player, permission, PermissionStatus.NOT_SET);
    }
    return setPlayerWorldPermission(player, permission, world, PermissionStatus.NOT_SET);
  }

  private boolean setPlayerPermission(
    String player,
    String permission,
    PermissionStatus status
  ) {
    var userId = findPlayerUniqueId(player);
    return userRepository.findUser(userId)
      .map(permissionUser -> permissionUser.setPermissionStatus(permission,
        status))
      .orElse(false);
  }

  private boolean setPlayerWorldPermission(
    String player,
    String permission,
    String world,
    PermissionStatus status
  ) {
    var userId = findPlayerUniqueId(player);
    return userRepository.findUser(userId)
      .map(permissionUser -> permissionUser.setWorldPermissionStatus(permission,
        status, world))
      .orElse(false);
  }

  @Override
  public boolean groupHas(String world, String group, String permission) {
    if (world == null) {
      return groupHas(group, permission);
    }
    return groupHasWorld(group, permission, world);
  }

  private boolean groupHas(String group, String permission) {
    var groupOptional = groupRepository.findGroup(group);
    return groupOptional
      .map(permissionGroup -> permissionGroup.hasPermissions(permission))
      .orElse(false);
  }

  private boolean groupHasWorld(String group, String permission, String world) {
    var groupOptional = groupRepository.findGroup(group);
    return groupOptional
      .map(permissionGroup -> permissionGroup.hasPermissions(permission, world))
      .orElse(false);
  }

  @Override
  public boolean groupAdd(String world, String group, String permission) {
    return false;
  }

  @Override
  public boolean groupRemove(String world, String group, String permission) {
    return false;
  }

  private boolean setGroupPermission(
    String group,
    String permission,
    PermissionStatus status
  ) {
    return groupRepository.findGroup(group)
      .map(permissionGroup -> permissionGroup.setPermissionStatus(
        permission,
        status
      ))
      .orElse(false);
  }

  private boolean setGroupWorldPermission(
    String group,
    String permission,
    String world,
    PermissionStatus status
  ) {
    return groupRepository.findGroup(group)
      .map(permissionGroup -> permissionGroup.setWorldPermissionStatus(
        permission,
        status, world
      ))
      .orElse(false);
  }

  @Override
  public boolean playerInGroup(String world, String player, String group) {
    String[] playerGroups = getPlayerGroups(world, player);
    return Arrays.asList(playerGroups).contains(group);
  }

  @Override
  public boolean playerAddGroup(String world, String player, String group) {
    var id = findPlayerUniqueId(player);
    var groupOptional = groupRepository.findGroup(group);
    if (groupOptional.isEmpty()) {
      return false;
    }
    var userOptional = userRepository.findUser(id);
    if (userOptional.isEmpty()) {
      return false;
    }
    var permissionUser = userOptional.get();
    var permissionGroup = groupOptional.get();
    return permissionUser.removeGroup(permissionGroup);
  }

  @Override
  public boolean playerRemoveGroup(String world, String player, String group) {
    var id = findPlayerUniqueId(player);
    var groupOptional = groupRepository.findGroup(group);
    if (groupOptional.isEmpty()) {
      return false;
    }
    var userOptional = userRepository.findUser(id);
    if (userOptional.isEmpty()) {
      return false;
    }
    var permissionUser = userOptional.get();
    var permissionGroup = groupOptional.get();
    return permissionUser.addGroup(permissionGroup);
  }

  @Override
  public String[] getPlayerGroups(String world, String player) {
    return userRepository.findUser(findPlayerUniqueId(player))
      .orElseThrow(
        () -> PermissionUserNotFoundException.withMessage("User not found")
      ).groups()
      .stream()
      .map(PermissionGroup::name)
      .toArray(String[]::new);
  }

  @Override
  public String getPrimaryGroup(String world, String player) {
    return getPlayerGroups(world, player)[0];
  }

  @Override
  public String[] getGroups() {
    return groupRepository.findAll().stream()
      .map(PermissionGroup::name)
      .toArray(String[]::new);
  }

  private static final boolean GROUP_SUPPORT = true;

  @Override
  public boolean hasGroupSupport() {
    return GROUP_SUPPORT;
  }

  private UUID findPlayerUniqueId(String player) {
    return UUID.randomUUID();
  }
}