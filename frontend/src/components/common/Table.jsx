import React from 'react';

export function Table({
  columns = [],
  data = [],
  isLoading = false,
  emptyMessage = 'No records found',
  onRowClick,
  className = '',
}) {
  return (
    <div className={`overflow-x-auto rounded-xl border border-[#EAD8C3] bg-[#FBF5EC] ${className}`}>
      <table className="w-full text-left text-sm text-[#342522]">
        <thead className="bg-[#F3E6D6]/80 text-xs uppercase font-semibold text-[#89766C] tracking-wider border-b border-[#EAD8C3]">
          <tr>
            {columns.map((col, index) => (
              <th
                key={col.key || index}
                scope="col"
                className={`px-5 py-3.5 ${col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left'} ${col.className || ''}`}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-[#EAD8C3]">
          {isLoading ? (
            <tr>
              <td colSpan={columns.length} className="px-5 py-12 text-center text-[#89766C]">
                <div className="flex flex-col items-center justify-center space-y-2">
                  <svg className="animate-spin h-6 w-6 text-[#541923]" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <p className="text-xs">Loading records...</p>
                </div>
              </td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="px-5 py-10 text-center text-sm text-[#89766C]">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row, rowIndex) => (
              <tr
                key={row.id || rowIndex}
                onClick={() => onRowClick && onRowClick(row)}
                className={`transition-colors ${
                  onRowClick ? 'cursor-pointer hover:bg-[#F3E6D6]/40' : 'hover:bg-[#F8EFE4]/60'
                }`}
              >
                {columns.map((col, colIndex) => {
                  const cellValue = col.render ? col.render(row[col.key], row, rowIndex) : row[col.key];
                  return (
                    <td
                      key={col.key || colIndex}
                      className={`px-5 py-3.5 whitespace-nowrap ${
                        col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : 'text-left'
                      } ${col.cellClassName || ''}`}
                    >
                      {cellValue}
                    </td>
                  );
                })}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default Table;
