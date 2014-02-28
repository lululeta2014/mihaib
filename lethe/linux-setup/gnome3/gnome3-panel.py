#! /usr/bin/env python3

# This is a little messy:
# Most is in ‘dconf’ (which can't be listed/dumped recursively)
# and one setting is in gsettings.
# Use ‘dconf-editor’ to look at data and ‘dconf watch /path/’ while you're
# adding items to see the changes.

# Panel alignment seems to work like this:
# panel-type ‘start’: pack-index 0 is left-most, pack-index 1 is next, etc.
# panel-type ‘end’: pack-index 0 is right-most, pack-index 1 is next, etc.

import os
import string
import subprocess
import sys


# Import from our own ‘util’ module
program_dir = os.path.dirname(sys.argv[0])
util_dir = os.path.join(os.path.dirname(os.path.abspath(program_dir)), 'util')
sys.path.append(util_dir)

from util import check_sourceme_bash_or_exit, gsettings_get, gsettings_set, \
        toGvariant, dconf_write, gconftool2_set

# check sourceme bash now
check_sourceme_bash_or_exit(os.path.dirname(os.path.abspath(program_dir)))


src_launchers_dir = os.path.join(os.path.dirname(os.path.abspath(program_dir)),
            'launchers')
dest_launchers_dir = \
        os.path.expanduser('~/.config/gnome-panel/mihaib-launchers')
launchers_templ_map = {name: os.getenv(name) for name in
        ('MB_PRG_DIR', 'MB_BYHAND_PRG_DIR', 'MB_OPERA_REG_PROF_PATH')}

# define top-panel and bottom-panel names
if (os.getenv('MB_LSB_ID')+'-'+os.getenv('MB_LSB_REL') in
        {'Ubuntu-13.04', 'Ubuntu-12.10', 'Ubuntu-12.04'}):
    top_panel_name = 'top-panel'
    bottom_panel_name = 'bottom-panel'
elif os.getenv('MB_LSB_ID') == 'Debian' and os.getenv('MB_LSB_CN') == 'wheezy':
    top_panel_name = 'top-panel'
    bottom_panel_name = 'bottom-panel'
else:
    top_panel_name = 'top-panel-0'
    bottom_panel_name = 'bottom-panel-0'


def warn_if_unknown_obj_list():
    if os.getenv('MB_LSB_ID') == 'Debian':
        if os.getenv('MB_LSB_CN') == 'wheezy':
            known = ['menu-bar', 'clock', 'notification-area',
                    'user-menu', 'window-list', 'workspace-switcher']
        else:
            known = ['menu-bar-0', 'clock-0', 'notification-area-0',
                    'user-menu-0', 'window-list-0', 'workspace-switcher-0']
    elif (os.getenv('MB_LSB_ID')+'-'+os.getenv('MB_LSB_REL') in
            {'Ubuntu-13.04', 'Ubuntu-12.10', 'Ubuntu-12.04'}):
        known = ['menu-bar', 'indicators', 'show-desktop', 'window-list',
                'workspace-switcher']
    else:
        known = ['menu-bar-0', 'indicators-0', 'show-desktop-0',
                'window-list-0', 'workspace-switcher-0']
    known = toGvariant(known)
    found = gsettings_get('org.gnome.gnome-panel.layout', 'object-id-list')
    if known != found:
        print(found, 'is different from', known,
                'You may want to update this script',
                sep='\n', file=sys.stderr)

    known = toGvariant([top_panel_name, bottom_panel_name])
    found = gsettings_get('org.gnome.gnome-panel.layout', 'toplevel-id-list')
    if known != found:
        print(found, 'is different from', known,
                'You may want to update this script',
                sep='\n', file=sys.stderr)


def delete_all_objects():
    subprocess.check_call(['dconf', 'reset', '-f',
        '/org/gnome/gnome-panel/layout/objects/'])


def make_panel_obj(name_list, name, iid, pack_index, pack_type, toplevel_id,
        inst_cfg = {}):
    name_list.append(name)
    objdir = '/org/gnome/gnome-panel/layout/objects/' + name + '/'
    for k, v in {
            'object-iid': iid,
            'pack-index': pack_index,
            'pack-type': pack_type,
            'toplevel-id': toplevel_id}.items():
        dconf_write(objdir + k, v)

    cfgdir = objdir + 'instance-config/'
    for k, v in inst_cfg.items():
        dconf_write(cfgdir + k, v)


def add_standard_items(obj_list):
    make_panel_obj(obj_list, 'menu-bar', 'PanelInternalFactory::MenuBar',
            0, 'start', top_panel_name)
    make_panel_obj(obj_list, 'user-menu', 'PanelInternalFactory::UserMenu',
            0, 'end', top_panel_name)
    make_panel_obj(obj_list, 'clock', 'ClockAppletFactory::ClockApplet',
            1, 'end', top_panel_name)
    config_clock('clock')
    make_panel_obj(obj_list, 'notification-area',
            'NotificationAreaAppletFactory::NotificationArea',
            2, 'end', top_panel_name)
    make_panel_obj(obj_list, 'window-list', 'WnckletFactory::WindowListApplet',
            0, 'start', bottom_panel_name)
    make_panel_obj(obj_list, 'workspace-switcher',
            'WnckletFactory::WorkspaceSwitcherApplet',
            0, 'end', bottom_panel_name,
            {'display-all-workspaces': True, 'num-rows': 1})
    make_panel_obj(obj_list, 'cpu-freq', 'CPUFreqAppletFactory::CPUFreqApplet',
            0, 'center', top_panel_name)
    make_panel_obj(obj_list, 'system-monitor',
            'MultiLoadAppletFactory::MultiLoadApplet',
            1, 'center', top_panel_name)
    config_system_monitor('system-monitor')


def make_launcher(obj_list, launcher, pack_index):
    src_path = os.path.join(src_launchers_dir, launcher + '.desktop')
    with open(src_path, 'r', encoding='utf-8') as f:
        templ = string.Template(f.read())

    dest_path = os.path.join(dest_launchers_dir, launcher + '.desktop')
    with open(dest_path, 'w', encoding='utf-8') as f:
        f.write(templ.substitute(launchers_templ_map))

    make_panel_obj(obj_list, 'launch-' + str(pack_index) + '-' + launcher,
            'PanelInternalFactory::Launcher', pack_index, 'start',
            top_panel_name, {'location': dest_path})


def add_launchers(obj_list):
    on_debian = os.getenv("MB_LSB_ID") == "Debian"
    private_computer = os.getenv("MB_PRIVATE_COMP") == "1"
    # number of item with pack-type 'start' on top-panel
    # 1 existing item: menu-bar
    n = 1

    make_launcher(obj_list, 'firefox' if not on_debian else 'iceweasel', n)
    n += 1

    if on_debian:
        if private_computer:
            chromium_launcher = 'chromium.debian'
        else:
            chromium_launcher = 'chromium.debian-foreign'
    else:
        if private_computer:
            chromium_launcher = 'chromium'
        else:
            chromium_launcher = 'chromium-foreign'
    make_launcher(obj_list, chromium_launcher, n)
    n += 1

    make_launcher(obj_list, 'opera', n)
    n += 1

    make_launcher(obj_list, 'utorrent', n)
    n += 1
    make_launcher(obj_list, 'npp', n)
    n += 1
    make_launcher(obj_list, 'eclipse', n)
    n += 1
    make_launcher(obj_list, 'gnome-system-monitor', n)
    n += 1
    make_launcher(obj_list, 'disk-usage-analyzer', n)
    n += 1
    if (os.getenv('MB_LSB_ID')+'-'+os.getenv('MB_LSB_REL') == 'Ubuntu-12.04' or
            os.getenv('MB_LSB_ID')+'-'+os.getenv('MB_LSB_CN')
            == 'Debian-wheezy'):
        make_launcher(obj_list, 'disk-utility', n)
        n += 1
    else:
        make_launcher(obj_list, 'gnome-disks', n)
        n += 1
    make_launcher(obj_list,
            'synaptic' if not on_debian else 'synaptic-debian', n)
    n += 1


def gconf2_set_panel_obj(objname, items):
    gconftool2_set('/apps/panel3-applets/' + objname.replace('-', '_'), items)


def config_clock(objname):
    if (os.getenv('MB_LSB_ID')+'-'+os.getenv('MB_LSB_REL') == 'Ubuntu-12.04' or
            os.getenv('MB_LSB_ID') == 'Debian'):
        gconf2_set_panel_obj(objname, {
            'format': '24-hour',
            'temperature_unit': 'C',
            'speed_unit': 'km/h',
            'expand_locations': True
            })
        subprocess.check_call(['gconftool-2', '--set',
            '/apps/panel3-applets/' + objname + '/cities',
            '--type', 'list', '--list-type=string',
            '[<location name="" city="Bucharest" timezone="Europe/Bucharest"' +
            'latitude="44.500000" longitude="26.133333" code="LRBS"' +
            'current="true"/>]'])
    else:
        gsettings_set('org.gnome.GWeather', 'temperature-unit', 'centigrade')
        gsettings_set('org.gnome.GWeather', 'speed-unit', 'kph')
        gsettings_set('org.gnome.desktop.interface', 'clock-format', '24h')
        gsettings_set('org.gnome.desktop.interface', 'clock-show-date', True)
        inst_cfg = ('/org/gnome/gnome-panel/layout/objects/' + objname +
                '/instance-config')
        dconf_write(inst_cfg + '/show-weather', True)
        dconf_write(inst_cfg + '/show-temperature', True)


def config_system_monitor(objname):
    items = {
            'cpuload_color0': '#00A3FF',
            'cpuload_color1': '#E60005',
            'cpuload_color2': '#FFFF00',
            'cpuload_color3': '#1BC800',
            'view_memload': True,
            'view_netload': True,
            'view_swapload': True,
            'view_diskload': True,
            'diskload_color0': '#24C600',
            }

    if (os.getenv('MB_LSB_ID')+'-'+os.getenv('MB_LSB_REL') != 'Ubuntu-12.04' or
            os.getenv('MB_LSB_ID') == 'Debian'):
        pass
    else:
        items.update({
            # background colors
            'cpuload_color4': '#4E4E4E',
            'memload_color4': '#4E4E4E',
            'netload2_color3': '#4E4E4E',
            'swapload_color1': '#4E4E4E',
            'diskload_color2': '#4E4E4E',
            })
    gconf2_set_panel_obj(objname, items)


if __name__ == '__main__':
    os.makedirs(dest_launchers_dir, exist_ok=True)
    warn_if_unknown_obj_list()
    delete_all_objects()

    obj_list = []
    add_standard_items(obj_list)
    add_launchers(obj_list)

    dconf_write('/org/gnome/gnome-panel/layout/object-id-list', obj_list)

    print('Logout & Login to reload the panel plugins',
            'to their correct positions')
