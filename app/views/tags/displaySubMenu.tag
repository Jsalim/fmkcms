#{if _menu}
<ul class="menu" id="&{_menu.name}-menu">
   #{list items:_menu.items, as:'item'}
   #{displayMenuItem menuItem:item, liCss:_liCss /}
   #{/list}
</ul>
#{/if}
