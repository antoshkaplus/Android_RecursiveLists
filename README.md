# Android_RecursiveLists

## Story
Sometimes I just need to write somewhere some kind of list.
Those lists sometimes grouped by category. And also we want to support subcategories. 
So recursive lists are very good for those things.

## Features
* reposition 
* deletion 
* renaming
* adding new
* recursion on items
* undo delete operations
* keep data on the server
* sync between multiple devices
* moving items in and out


## Usage
* long press on list item or free space on activity and follow options
* in action bar options find action to undo removal operations

## Bugs
* application goes down after pressing word duplicates
* application goes down after try to reposition in empty bottom space
* application goes down after rotation with dialog box add item on

## TODO 
* when user installs application we provide him with lots of initial data. 
but he may already have it on the cloud. solution : 
1) dialog box to ask if he needs initial data loaded
2) create some predefined id-s for initial data to make it unique and the same for all devices
* clear all removed items but leave 10 or another count that are most recent
* purge ???


Screenshots

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
