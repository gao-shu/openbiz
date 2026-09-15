package com.openbiz.service.port;

/**
 * Validates assignee against RuoYi sys_user without owning Employee table.
 */
public interface StaffUserPort
{
    void requireActiveUser(Long userId);
}
