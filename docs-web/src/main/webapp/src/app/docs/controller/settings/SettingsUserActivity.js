'use strict';

/**
 * Settings user activity controller.
 */
angular.module('docs').controller('SettingsUserActivity', function($scope, Restangular, $translate) {
  // Default period is one week
  $scope.selectedPeriod = $translate.instant('settings.useractivity.period_week');
  $scope.ganttTasks = [];

  // Load charts library
  var loadChartJs = function() {
    if (typeof Chart !== 'undefined') {
      initCharts();
      return;
    }
    
    // Dynamically load Chart.js if needed
    var script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/chart.js';
    script.onload = function() {
      initCharts();
    };
    document.head.appendChild(script);
  };
  
  // Load Gantt library
  var loadGanttChart = function() {
    if (typeof google !== 'undefined' && typeof google.charts !== 'undefined') {
      initGanttChart();
      return;
    }
    
    // Dynamically load Google Charts if needed
    var script = document.createElement('script');
    script.src = 'https://www.gstatic.com/charts/loader.js';
    script.onload = function() {
      google.charts.load('current', {'packages':['gantt']});
      google.charts.setOnLoadCallback(initGanttChart);
    };
    document.head.appendChild(script);
  };
  
  /**
   * Initialize charts with data.
   */
  var initCharts = function() {
    // User activity chart
    if ($scope.userActivityData) {
      var ctx = document.getElementById('userActivityChart').getContext('2d');
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: $scope.userActivityData.labels,
          datasets: [{
            label: $translate.instant('settings.useractivity.activity_count'),
            data: $scope.userActivityData.data,
            backgroundColor: 'rgba(54, 162, 235, 0.5)'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    }
    
    // Activity type chart
    if ($scope.activityTypeData) {
      var ctx2 = document.getElementById('activityTypeChart').getContext('2d');
      new Chart(ctx2, {
        type: 'pie',
        data: {
          labels: $scope.activityTypeData.labels,
          datasets: [{
            data: $scope.activityTypeData.data,
            backgroundColor: [
              'rgba(255, 99, 132, 0.5)',
              'rgba(54, 162, 235, 0.5)',
              'rgba(255, 206, 86, 0.5)',
              'rgba(75, 192, 192, 0.5)',
              'rgba(153, 102, 255, 0.5)'
            ]
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false
        }
      });
    }
  };
  /**
   * Initialize Gantt chart with document progress data.
   */
  var initGanttChart = function() {
    if (!$scope.ganttTasks || !$scope.ganttTasks.length) return;
    
    var container = document.getElementById('ganttChart');
    var chart = new google.visualization.Gantt(container);
    
    var data = new google.visualization.DataTable();
    data.addColumn('string', 'Task ID');
    data.addColumn('string', 'Document Title');
    data.addColumn('string', 'Language');
    data.addColumn('date', 'Create Date');
    data.addColumn('date', 'Last Update');
    data.addColumn('number', 'Duration');
    data.addColumn('number', 'Completion');
    data.addColumn('string', 'Dependencies');
    
    $scope.ganttTasks.sort(function(a, b) {
      var dateA = a.startDate;
      var dateB = b.startDate;
      return dateB - dateA; 
    });
    
    // Add rows from our data
    var rows = $scope.ganttTasks.map(function(task) {
    
      // Define color based on age category
      var barColor;
      switch(task.ageCategory) {
        case $translate.instant('settings.useractivity.recent'):
          barColor = '#4285f4'; // Blue
          break;
        case $translate.instant('settings.useractivity.this_month'):
          barColor = '#0f9d58'; // Green
          break;
        case $translate.instant('settings.useractivity.quarter'):
          barColor = '#f4b400'; // Yellow
          break;
        default:
          barColor = '#db4437'; // Red
      }
      
      return [
        task.id,
        task.name,
        task.resource,
        task.startDate,
        task.endDate,
        null,
        task.percentComplete,
        null,
        barColor  // Add the color as styling information
      ];
    });
    data.addColumn('string', 'Style');
    data.addRows(rows);
    
    var options = {
      gantt: {
        defaultStartDate: new Date($scope.ganttTasks[0].startDate), 
        trackHeight: 40, 
        barHeight: 30, 
        shadowEnabled: false, 
        labelStyle: {
          fontName: '"Helvetica Neue", Helvetica, Arial, sans-serif',
          fontSize: 12
        },
        barCornerRadius: 3, 
        innerGridHorizLine: {
          stroke: '#ddd',
          strokeWidth: 0.5
        },
        palette: [
          {
            "color": "#4285f4",
            "dark": "#3367d6",
            "light": "#d0e0fc"
          },
          {
            "color": "#0f9d58",
            "dark": "#0b8043",
            "light": "#c6e7d1"
          },
          {
            "color": "#f4b400",
            "dark": "#f09300",
            "light": "#fce8b2"
          },
          {
            "color": "#db4437",
            "dark": "#c53929",
            "light": "#f5c2bd"
          }
        ],
        innerGridTrack: {fill: '#f9f9f9'},
        innerGridDarkTrack: {fill: '#f5f5f5'}
      }
    };
    
    chart.draw(data, options);

    $(window).resize(function() {
      chart.draw(data, options);
    });
  };
  /**
   * Set the time period for activity data.
   */
  $scope.setPeriod = function(period) {
    switch(period) {
      case 'day':
        $scope.selectedPeriod = $translate.instant('settings.useractivity.period_day');
        break;
      case 'month':
        $scope.selectedPeriod = $translate.instant('settings.useractivity.period_month');
        break;
      default:
        $scope.selectedPeriod = $translate.instant('settings.useractivity.period_week');
        period = 'week';
    }
    
    loadUserActivityData(period);
  };
  
  /**
   * Load user activity data from server.
   */
  var loadUserActivityData = function(period) {
    Restangular.one('auditlog').get({
      limit: 1000,
      period: period
    }).then(function(data) {
      processUserActivityData(data.logs);
    });
    
    // Also load document progress data for Gantt chart
    loadDocumentProgressData(period);
  };
  
  /**
   * Process user activity data for charts.
   */
  var processUserActivityData = function(logs) {
    // Group by user
    var userActivity = {};
    // Group by activity type
    var activityTypes = {};
    
    logs.forEach(function(log) {
      // Count by user
      if (!userActivity[log.username]) {
        userActivity[log.username] = 0;
      }
      userActivity[log.username]++;
      
      // Count by activity type
      var type = log.class + '-' + log.type;
      if (!activityTypes[type]) {
        activityTypes[type] = 0;
      }
      activityTypes[type]++;
    });
    
    // Prepare data for user activity chart
    $scope.userActivityData = {
      labels: Object.keys(userActivity),
      data: Object.values(userActivity)
    };
    
    // Prepare data for activity type chart
    $scope.activityTypeData = {
      labels: Object.keys(activityTypes).map(function(key) {
        var parts = key.split('-');
        return $translate.instant('directive.auditlog.' + parts[0]) + ' - ' + 
               $translate.instant('directive.auditlog.log_' + parts[1].toLowerCase());
      }),
      data: Object.values(activityTypes)
    };
    
    // Initialize charts
    loadChartJs();
  };
  
  /**
   * Load document progress data for Gantt chart.
   */
  var loadDocumentProgressData = function(period) {
    Restangular.one('document/list').get({
      limit: 50,
      sort_column: 3,
      asc: false
    }).then(function(data) {
      processDocumentProgressData(data.documents);
    });
  };
  
  /**
   * Process document data for Gantt chart.
   */
  var processDocumentProgressData = function(documents) {
    var tasks = [];
    
    // Current date as reference
    var now = new Date();
    
    documents.forEach(function(doc) {
      var createDate = new Date(doc.create_date);
      var updateDate = doc.update_date ? new Date(doc.update_date) : now;
      
      // Calculate document age in days
      var documentAge = (now - createDate) / (1000 * 60 * 60 * 24);
      var documentAgeCategory = getAgeCategory(documentAge);
      
      var task = {
        id: doc.id,
        name: doc.title || $translate.instant('settings.useractivity.untitled'),
        resource: doc.language || 'unknown', 
        startDate: createDate,
        endDate: updateDate,
        percentComplete: calculateCompletion(doc),
        ageCategory: documentAgeCategory 
      };
      
      tasks.push(task);
    });

    tasks.sort(function(a, b) {
      var ageOrder = {
        'settings.useractivity.recent': 1,
        'settings.useractivity.this_month': 2,
        'settings.useractivity.quarter': 3,
        'settings.useractivity.older': 4
      };
      
      return ageOrder[a.ageCategory] - ageOrder[b.ageCategory];
    });
    
    $scope.ganttTasks = tasks;
    
    if ($scope.ganttTasks.length > 0) {
      loadGanttChart();
    }
  };

  /**
 * Get age category based on document age in days.
 */
  var getAgeCategory = function(days) {
    if (days < 7) return $translate.instant('settings.useractivity.recent');
    if (days < 30) return $translate.instant('settings.useractivity.this_month');
    if (days < 90) return $translate.instant('settings.useractivity.quarter');
    return $translate.instant('settings.useractivity.older');
  };

  /**
   * Calculate document completion status with more granularity.
   * This provides a more accurate representation of document completeness
   * by considering multiple factors and weighting them.
   */
  var calculateCompletion = function(doc) {
    let completionScore = 0;
    const maxScore = 100;
    
    // Base score - document exists
    completionScore += 10;
    
    // Check title and description
    if (doc.title && doc.title.trim().length > 0) {
      completionScore += 15;
    }
    
    if (doc.description && doc.description.trim().length > 0) {
      completionScore += 10;
    }
    
    // Check file attachments
    if (doc.file_count > 0) {
      // More files suggest a more complete document
      completionScore += Math.min(20, doc.file_count * 5);
    }
    
    // Check update history
    if (doc.update_date && doc.create_date !== doc.update_date) {
      completionScore += 15;
      
      // Additional points if recently updated (within last week)
      const lastUpdate = new Date(doc.update_date);
      const now = new Date();
      const daysSinceUpdate = (now - lastUpdate) / (1000 * 60 * 60 * 24);
      
      if (daysSinceUpdate < 7) {
        completionScore += 10;
      }
    }
    
    // Check for tags/categories
    if (doc.tags && doc.tags.length > 0) {
      completionScore += Math.min(10, doc.tags.length * 2);
    }
    
    // Language specified
    if (doc.language && doc.language !== 'unknown') {
      completionScore += 5;
    }
    
    // Ensure we don't exceed 100%
    return Math.min(completionScore, maxScore);
  };

  // Initial data load with default period (week)
  loadUserActivityData('week');
});