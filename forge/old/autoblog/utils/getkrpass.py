#!/usr/bin/env python

import argparse
import getpass
import gnomekeyring

def parse_args():
    parser = argparse.ArgumentParser(
            description='''Retrieve password from keyring and print it
            to stdout optionally creating both keyring and item if missing.

            You need to have your default (or 'login') keyring unlocked.
            If you use auto-login, you don't have to type your password and
            the keyring doesn't get unlocked.

            This is a tool called by the main program who reads its stdout.
            If the program needs any user input (e.g. to store the keyring
            or item) getpass.getpass(..) sees that stdout is not controlling
            the terminal and uses stderr.
            ''')
    parser.add_argument('keyring')
    parser.add_argument('item_name')
    return parser.parse_args()

def get_secret(keyring, item_name):
    for id in gnomekeyring.list_item_ids_sync(keyring):
        item = gnomekeyring.item_get_info_sync(keyring, id)
        if item.get_display_name() == item_name:
            return item.get_secret()

def store_secret(keyring, item_name, secret):
    # The last 3 params are: attributes, secret, update_if_exists.
    # from the docs: "update_if_exists:  If true, then another item matching
    # the type, and attributes will be updated instead of creating a new item."
    # So we either store unique attributes with each, or set update_if_exists
    # to False.
    # If we do both, additional keys with the same name and different IDs
    # will get created.
    gnomekeyring.item_create_sync(keyring, gnomekeyring.ITEM_GENERIC_SECRET,
            item_name, {'name': item_name}, secret, True)

if __name__ == '__main__':
    args = parse_args()

    '''Sometimes the 'login' keyring is not automatically marked as default
    (the user needs to do it manually). If no keyring is marked as default,
    fall back to the 'login' keyring.'''
    default_keyring = gnomekeyring.get_default_keyring_sync() or 'login'

    keyring_pass = get_secret(default_keyring, args.keyring)
    if not keyring_pass:
        keyring_pass = getpass.getpass(
                'New password for keyring ' + args.keyring + ': ')
        gnomekeyring.create_sync(args.keyring, keyring_pass)
        store_secret(default_keyring, args.keyring, keyring_pass)

    gnomekeyring.unlock_sync(args.keyring, keyring_pass)
    secret = get_secret(args.keyring, args.item_name)
    if not secret:
        secret = getpass.getpass('New password for ' + args.item_name + ': ')
        store_secret(args.keyring, args.item_name, secret)
    print(secret)
