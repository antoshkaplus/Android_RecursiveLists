# Android_RecursiveLists

## Story
Sometimes I just need to write somewhere some kind of list.
Those lists sometimes grouped by category. And also we want to support subcategories. 
So recursive lists are very good for those things.

Now it's also about keeping your tasks there. We decided not to write multiple applications,
but use one.


### Final version for offline only usage

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

## TODO
* item creation date
* unite removed and usual items

* if you have task group store them inside an item. 
* if you have task and subtasks, store them inside a task. this big task can be completed only after all leaf tasks are completed
* you can't create items inside a task
* have a setting that shows for how long to show completed tasks.


### App
# FIX ACCOUNT PICKING. DO IT SMART.

* make synchronization process better (use tips from words application)
* when user installs application we provide him with lots of initial data.
but he may already have it on the cloud. solution :
1) dialog box to ask if he needs initial data loaded
2) create some predefined id-s for initial data to make it unique and the same for all devices
* clear all removed items but leave 10 or another count that are most recent
* purge ???
* add task lists. right can be as usual lists but may need improvement later on.
even right now need ability to mark as completed. list containters should probably have properties too.
different classes.

### Backend



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
