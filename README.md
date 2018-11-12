# Android_RecursiveLists

## Story
Sometimes I just need to write somewhere some kind of list.
Those lists sometimes grouped by category. And also we want to support subcategories. 
So recursive lists are very good for those things. You can think of it as a big tree full of different items.

Also people like to make lists of tasks they want to complete. And usually those are also divided on categories.
Sometimes you may want to have a task that consists of multiple small tasks.

With tasks we can implement different statistics to show user his productivity. 

## What's New
* We are working on web app

## Bugs
* Not ready

## Features
* Not ready

## TODO Android App

### Features
* reposition
* deletion
* renaming
* adding new
* recursion on items
* undo delete operations
* keep data on the server
* sync between multiple devices
* moving items in and out

### Usage
* long press on list item or free space on activity and follow options
* in action bar options find action to undo removal operations

### App
### FIX ACCOUNT PICKING. DO IT SMART.
* make synchronization process better (use tips from words application)
* when user installs application we provide him with lots of initial data.
but he may already have it on the cloud. solution :
    * dialog box to ask if he needs initial data loaded
    * create some predefined id-s for initial data to make it unique and the same for all devices
* clear all removed items but leave 10 or another count that are most recent
* purge ???
* add task lists. right can be as usual lists but may need improvement later on.
even right now need ability to mark as completed. list containters should probably have properties too.
different classes.

## TODO Logic
* unite removed and usual items
* have a setting that shows for how long to show completed tasks.

## TODO Web
* hashtags: on save let user to add tags of directories on current path or maybe just last directory.
remember user hashtags to give suggestions
* edit dialog
* distinguish between leaf and non-leaf items. show button for adding nestiness.
* think about showing items created date, maybe as "hover description"

## Old Android App Screenshots

<table>
  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/antoshkaplus/Android_RecursiveLists/master/screenshots/root_list.png" />
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/antoshkaplus/Android_RecursiveLists/master/screenshots/context_menu.png" />
    </td>
  </tr>
</table>
